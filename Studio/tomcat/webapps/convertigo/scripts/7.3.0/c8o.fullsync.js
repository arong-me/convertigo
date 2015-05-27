$.extend(true, C8O, {
	init_vars: {
		fs_server: null,
		fs_force_pouch: false,
		fs_force_pouch_replication: false
	},
	
	vars: {
		fs_default_db: null,
		fs_default_design: null,
		fs_token: null
	},
	
	_define: {
		re_fs_match_db: new RegExp("^fs://([^/]*)"),
		re_fs_seq: new RegExp("^(.*?)(?:#|$)"),
		re_fs_task: new RegExp("(\\d+)[^\\d]*(\\d+)"),
		re_fs_use: new RegExp("^(?:_use_(.*)$|__)")
	},
	
	_fs: {
		server: null,
		live_ids: {},
		live_views: {},
		live_dbs: {},
		syncs: {},
		sync_events: ["change", /*"pause", "active", "denied",*/ "complete", "error"],
		dbs: {},
		
		handle: function (err, doc, callback) {
			if (err) {
				C8O.log.error("c8o.fs  : an error occurs", err);
			}
			callback(doc);
		},
		
		getDb: function (db) {
			if (C8O._fs.dbs[db]) {
				// exists
			} else if (C8O._fs.server && !C8O.init_vars.fs_force_pouch) {
				C8O._fs.dbs[db] = new PouchDB(C8O._fs.server + '/' + db, {
					ajax: {timeout: 0} // disable PouchDB request timeout
				});
			} else {
				C8O._fs.dbs[db] = new PouchDB(db);
			}
			return C8O._fs.dbs[db];
		},
		
		getAllDocs: function (db, options, callback) {
			C8O.log.info("c8o.fs  : getAllDocs " + db + " " + C8O.toJSON(options));
			
			C8O._fs.getDb(db).allDocs(options, function (err, doc) {
				C8O._fs.handle(err, doc, callback);
			});
		},
		
		getDocument: function (db, docid, options, callback) {
			C8O.log.info("c8o.fs  : getDocument " + db + " " + docid + " " + C8O.toJSON(options));
			
			C8O._fs.handleOptionsRev(db, docid, options, false, function (options) {
				C8O._fs.getDb(db).get(docid, options, function (err, doc) {
					C8O._fs.handle(err, doc, callback);
				});
			});
		},
		
		getDocumentRev: function (db, docid, callback) {
			C8O._fs.getDb(db).get(docid, {}, function (err, doc) {
				callback(doc._rev);
			});
		},
		
		postDocument: function (db, document, policy, options, callback) {
			if (C8O.canLog("debug")) {
				C8O.log.debug("c8o.fs  : postDocument " + db + " " + C8O.toJSON(document) + " " + policy + " " + C8O.toJSON(options));
			} else {
				C8O.log.info("c8o.fs  : postDocument " + db + " (debug to see doc) " + policy + " " + C8O.toJSON(options));
			}
			
			C8O._fs.applyPolicy(db, document, policy, function (document) {
				C8O._fs.getDb(db).post(document, options, function (err, doc) {
					C8O._fs.handle(err, doc, callback);
				});
			});
		},
		
		deleteDocument: function (db, docid, rev, options, callback) {
			C8O.log.info("c8o.fs  : deleteDocument " + db + " " + docid + " " + rev + " " + C8O.toJSON(options));
			
			C8O._fs.handleRev(db, docid, rev, true, function (rev) {
				C8O._fs.getDb(db).remove(docid, rev, options, function (err, doc) {
					C8O._fs.handle(err, doc, callback);
				});
			});
		},
		
		getView: function (db, ddoc, view, options, callback) {
			C8O.log.info("c8o.fs  : getView " + db + " " + ddoc + " " + view + " " + C8O.toJSON(options));
			
			C8O._fs.getDb(db).query(ddoc + "/" + view, options, function (err, doc) {
				C8O._fs.handle(err, doc, callback);
			});
		},
		
		onChange: function (db, onChange) {
			C8O._fs.getDb(db).changes({
			  since: "now",
			  live: true
			}).on("change", onChange);
		},
		
		getRemoteDB: function (db) {
			return new PouchDB(C8O._fs.getRemoteUrl(db));
		},
		
		getRemoteUrl: function (db) {
			return C8O._fs.remote + (C8O.vars.fs_token ? "/~" + C8O.vars.fs_token : "") + "/" + db;
		},
				
		applyPolicy: function (db, document, policy, callback) {
			if (policy == "none") {
				// don't modify
				callback(document);
			} if (policy == "create") {
				delete document._id;
				delete document._rev;
				
				callback(document);
			} else {
				var docid = document._id || null;
				
				if (docid != null) {
					if (policy == "override") {
						C8O._fs.getDocumentRev(db, docid, function (rev) {
							if (rev != null) {
								document._rev = rev;
							}
							callback(document);
						});
					} else if (policy == "merge") {
						C8O._fs.getDocument(db, docid, {}, function (dbDocument) {
							if (dbDocument._id) {
								// merge documents
								delete document._rev;
								callback($.extend(true, dbDocument, document));
							}
						});
					}
				}
			}
		},
		
		handleOptionsRev: function (db, docid, options, addLast, callback) {			
			var rev = C8O._fs.handleRev(db, docid, options.rev, addLast, function (rev) {
				if (rev) {
					options.rev = rev;
				}
				callback(options);
			});
		},
		
		handleRev: function (db, docid, rev, addLast, callback) {
			if (!rev && addLast) {
				C8O._fs.getDocumentRev(db, docid, callback);
			} else {
				callback(rev);
			}
		},
		
		addLiveId: function (db, data) {
			if (data.__live) {
				C8O._fs.addLiveDb(db);
				C8O._fs.live_ids[data.docid] = $.extend({}, data);
				delete C8O._fs.live_ids[data.docid].__live;
				C8O._fs.live_ids[data.docid].__fromLive = true;
			}
		},
		
		addLiveView: function (db, data) {
			if (data.__live) {
				var key = data.ddoc + "/" + data.view;
				C8O._fs.addLiveDb(db);
				C8O._fs.live_views[key] = $.extend({}, data);
				delete C8O._fs.live_views[key].__live;
				C8O._fs.live_views[key].__fromLive = true		
			}
		},
		
		addLiveDb: function (db) {
			if (!C8O._fs.live_dbs[db]) {
				C8O._fs.live_dbs[db] = true;
				C8O._fs.onChange(db, function (change) {
					var data = C8O._fs.live_ids[change.id];
					if (data) {
						C8O.call(data);
					}
					for (var key in C8O._fs.live_views) {
						C8O.call(C8O._fs.live_views[key]);
					}
				});
			}
		},
		
		replicate: function (options, isPull) {
			options = $.extend({}, options);
			var db = C8O._remove(options, "db") || C8O.vars.fs_default_db;
			var direction = isPull ? "pull" : "push";
			var key = db + "_" + direction;
			
			if (C8O._fs.syncs[key]) {
				C8O._fs.syncs[key].cancel();
				delete C8O._fs.syncs[key];
			}
			
			if (C8O.isTrue(options.cancel)) {
				C8O.log.info("c8o.fs  : replicate canceled");
				return;
			}
			
			var local = C8O.fs_getDB(db);
			var remote = C8O._fs.getRemoteDB(db);
			
			var source = isPull ? remote : local;
			
			var evts = {
				complete: function (){},
				change: function (){},
				error: function (){}
			}
			
			var cancel = null;
			
			source.info().then(function (info) {
				var max = info.update_seq;
				var live = C8O.isTrue(options.live);
				options.live = false;
				
				if (local.type() == "http" && !C8O.init_vars.fs_force_pouch_replication) {
					var body = {
						source: isPull ? C8O._fs.getRemoteUrl(db) : db + "_device",
						target: !isPull ? C8O._fs.getRemoteUrl(db) : db + "_device",
					};
					var replicate = false;
					
					(isPull ? local : remote).info().then(function (info_target) {
						var total = Math.max(max - info_target.update_seq, 0);
						var lastCurrent = 0;
						
						var chkTasks = function () {
							local.request({method: "GET", url: "../_active_tasks"}, function (err, tasks) {
								if (err) {
									evts.error(err);
									return;
								}
								
								var task = null;
								for (var i = 0; i < tasks.length && task == null; i++) {
									if (tasks[i].source == body.source && tasks[i].target == body.target) {
										task = tasks[i];
									}
								}
								
								if (!replicate) {
									if (task) {
										local.request({method: "POST", url: "../_replicate", body: $.extend({}, body, {cancel: true})});
										C8O.log.info("c8o.fs  : cancel a previous replication for " + C8O.toJSON(body));
									} else {
										// async for Android, sync for IOs
										local.request({method: "POST", url: "../_replicate", body: body});
										C8O.log.info("c8o.fs  : replication started for " + C8O.toJSON(body));
										replicate = true;
									}
									window.setTimeout(chkTasks, 250);
								} else {
									if (task == null) {
										C8O.log.info("c8o.fs  : replication finished for " + C8O.toJSON(body));
										
										evts.complete({
											ok : true,
											direction: direction
										});
										
										if (live) {
											body.continuous = true;
											local.request({method: "POST", url: "../_replicate", body: body}, function (err, data) {
												C8O.log.info("c8o.fs  : continuous replication started for " + C8O.toJSON(body));
											});
										}
									} else {
										var current = lastCurrent;
										
										if (task.status) {
											var mStatus = task.status.match(C8O._define.re_fs_task);
											if (mStatus) {
												current = mStatus[1] * 1;
												total = Math.max(total, mStatus[2] * 1);
											}
										}
																				
										if (current != lastCurrent) {
											var progress = current / total * 100;
											
											evts.change({
												direction: direction,
												current: current,
												total: total,
												progress: progress,
												ok: true												
											});
										}
										
										lastCurrent = current;
										
										window.setTimeout(chkTasks, 750);
									}
								}
							});
						};
						
						chkTasks();
					});
					
					cancel = function () {
						body.cancel = true;
						local.request({method: "POST", url: "../_replicate", body: body}, function (err, data) {
							C8O.log.info("c8o.fs  : continuous replication cancel for " + C8O.toJSON(body));
						});
					};
				} else {
					var rep = local.replicate[isPull ? "from" : "to"](remote, options).on("change", function (change) {
						var min = change.last_seq - change.docs_written;
						var current = change.docs_written;
						var total = Math.max(max - min, 1);
						var progress = current / total * 100;
						
						evts.change({
							direction: direction,
							current: current,
							total: total,
							progress: progress,
							ok: true
						});
					}).on("complete", function () {
						evts.complete({
							ok : true,
							direction: direction
						});
						if (live) {
							options.live = live;
							cancel = local.replicate[isPull ? "from" : "to"](remote, options).cancel;
						}
					}).on("error", function (err) {
						evts.error($.extend({}, err, {direction: direction}));
					});
					
					cancel = function () {
						rep.cancel();
					};
				}
			});
			
			return C8O._fs.syncs[key] = {
				on: function (name, handler) {
					if (typeof handler == "function") {
						evts[name] = handler;
					}
					return this;
				},
				cancel: function () {
					if (cancel) {
						cancel();
					}
				}
			};
		}
	},
	
	fs_sync: function (options) {
		C8O.log.info("c8o.fs  : fs_replicate_sync requested");
		
		var pull = C8O._fs.replicate(options, true);
		var push = C8O._fs.replicate(options, false);
		
		var completes = {
			pull: null,
			push: null
		};
		
		var complete = function () {};
		
		pull.on("complete", function (data) {
			completes.pull = data;
			if (completes.push) {
				complete(completes);
			}
		});
		
		push.on("complete", function (data) {
			completes.push = data;
			if (completes.pull) {
				complete(completes);
			}
		});
		
		return {
			on: function (name, handler) {
				if (typeof handler == "function") {
					if (name == "complete") {
						complete = handler;
					} else {
						pull.on(name, handler);
						push.on(name, handler);
					}
				}
			},
			cancel: function () {
				pull.cancel();
				push.cancel();
			}
		}
	},
	
	fs_replicate_pull: function (options) {
		C8O.log.info("c8o.fs  : fs_replicate_pull requested");
		return C8O._fs.replicate(options, true);
	},
	
	fs_replicate_push: function (options) {
		C8O.log.info("c8o.fs  : fs_replicate_pull requested");
		return C8O._fs.replicate(options, false);
	},
	
	fs_onChange: function (options) {
		var db = options.db || C8O.vars.fs_default_db;
		C8O._fs.onChange(db + "_device", options.onChange);
	},
		
	fs_getDB: function (db) {
		db = (db || C8O.vars.fs_default_db) + "_device";
		return C8O._fs.getDb(db);
	}
});

C8O._init.locks.fullsync = true;
C8O._init.tasks.push(function () {
    C8O.log.info("c8o.fs  : initializing FullSync");
    
    C8O._fs.server = C8O._remove(C8O.init_vars, "fs_server");
    C8O._fs.remote = C8O._define.convertigo_path + "/fullsync";
    
    delete C8O._init.locks.fullsync;
    
    C8O._init.check();
});

C8O.addHook("_call_fs", function (data) {
	var db;
	if ((db = C8O._define.re_fs_match_db.exec(data.__project)) != null) {
		db = (db[1] ? db[1] : C8O.vars.fs_default_db) + "_device";
		C8O.log.debug("c8o.fs  : database used '" + db + "'");
		
		var callback = function (json) {
			if (C8O.canLog("trace")) {
				C8O.log.trace("c8o.fs  : json response\n" + JSON.stringify(json));
			}
			
			var xmlData = $.parseXML("<couchdb_output/>");
			C8O._jsonToXml(undefined, json, xmlData.documentElement, function (keys, json) {
				keys.sort();
				return keys;
			});
			
			if (C8O.canLog("debug")) {
				C8O.log.debug("c8o.fs  : xml response\n" + C8O.serializeXML(xmlData));
			}
			
			var fakeXHR = {
				C8O_data: data
			};
			
			C8O._onCallComplete(fakeXHR, "success");
			C8O._onCallSuccess(xmlData, "success", fakeXHR);
		};
		
		if (data.__sequence) {
			var options = {};
			var postData = {};
			
			for (var key in data) {
				var isUse = C8O._define.re_fs_use.exec(key); 
				if (isUse) {
					if (isUse[1]) {
						options[isUse[1]] = data[key];
					}
				} else {
					postData[key] = data[key];
				}
			}
			
			var seq = C8O._define.re_fs_seq.exec(data.__sequence)[1];
			C8O.log.info("c8o.fs  : calling " + seq);
			
			if (seq == "post") {
				var policy = C8O._remove(options, "policy") || "none";
				
				C8O._fs.postDocument(db, postData, policy, options, callback);
			} else {
				options = $.extend(postData, options);
				
				if (seq == "get") {
					C8O._fs.addLiveId(db, data);
					C8O._fs.getDocument(db, C8O._remove(options, "docid"), options, callback);
				} else if (seq == "delete") {
					C8O._fs.deleteDocument(db, C8O._remove(options, "docid"), C8O._remove(options, "rev"), options, callback);
				} else if (seq == "view") {
					C8O._fs.addLiveView(db, data);
					
					var ddoc = C8O._remove(options, "ddoc") || C8O.vars.fs_default_design;
					var view = C8O._remove(options, "view");
					
					C8O._fs.getView(db, ddoc, view, options, callback);
				} else if (seq == "all") {
					C8O._fs.getAllDocs(db, options, callback);
				} else if (seq == "sync" || seq == "replicate_pull" || seq == "replicate_push") {
					var callbacks = {};
					$.each(C8O._fs.sync_events, function() {
						var event = this;
						if (C8O.isTrue(C8O._remove(options, event))) {
							callbacks[event] = function (data) {
								callback({
									event: event,
									data: data
								});
							};
						}
					});
					
					var sync = C8O["fs_" + seq](options);
					
					if ($.isEmptyObject(callbacks)) {
						callback({
							event: "none",
							data: {}
						});
					} else {
						for (var key in callbacks) {
							sync.on(key, callbacks[key]);
						}
					}
				} else {
					callback({error: "invalid command '" + data.__sequence + "'"});
				}
			}
		}
		return false;
	}
});

$.support.cors = true;