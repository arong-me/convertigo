import { Component, Input, Output, EventEmitter, ViewChild, forwardRef, ElementRef, Renderer2, ViewEncapsulation } from '@angular/core';
import { TreeComponent, TreeNode, TreeModel, TREE_ACTIONS, KEYS, IActionMapping, ITreeOptions } from 'angular-tree-component';

// Note: due to styleUrls path problems, we use embedded styles notation  

@Component({
    selector: 'c8o-treeview',
    styles: [
    `   
.tree-children.tree-children-no-padding { padding-left: 0 }
.tree-children { padding-left: 20px; overflow: hidden }
.node-drop-slot { display: block; height: 2px }
.node-drop-slot.is-dragging-over { background: #ddffee; height: 20px; border: 2px dotted #888; }
.toggle-children-wrapper-expanded .toggle-children { transform: rotate(90deg) }
.toggle-children-wrapper-collapsed .toggle-children { transform: rotate(0); }
.toggle-children-wrapper {
  padding: 2px 3px 5px 1px;
}
/* tslint:disable */
.toggle-children {
  background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAASCAYAAABSO15qAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAABAhpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMDY3IDc5LjE1Nzc0NywgMjAxNS8wMy8zMC0yMzo0MDo0MiAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1wTU06T3JpZ2luYWxEb2N1bWVudElEPSJ1dWlkOjY1RTYzOTA2ODZDRjExREJBNkUyRDg4N0NFQUNCNDA3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOkYzRkRFQjcxODUzNTExRTU4RTQwRkQwODFEOUZEMEE3IiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOkYzRkRFQjcwODUzNTExRTU4RTQwRkQwODFEOUZEMEE3IiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE1IChNYWNpbnRvc2gpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MTk5NzA1OGEtZDI3OC00NDZkLWE4ODgtNGM4MGQ4YWI1NzNmIiBzdFJlZjpkb2N1bWVudElEPSJhZG9iZTpkb2NpZDpwaG90b3Nob3A6YzRkZmQxMGMtY2NlNS0xMTc4LWE5OGQtY2NkZmM5ODk5YWYwIi8+IDxkYzp0aXRsZT4gPHJkZjpBbHQ+IDxyZGY6bGkgeG1sOmxhbmc9IngtZGVmYXVsdCI+Z2x5cGhpY29uczwvcmRmOmxpPiA8L3JkZjpBbHQ+IDwvZGM6dGl0bGU+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+5iogFwAAAGhJREFUeNpiYGBgKABigf///zOQg0EARH4A4gZyDIIZ8B/JoAJKDIDhB0CcQIkBRBtEyABkgxwoMQCGD6AbRKoBGAYxQgXIBRuZGKgAKPIC3QLxArnRSHZCIjspk52ZKMrOFBUoAAEGAKnq593MQAZtAAAAAElFTkSuQmCC');
  height: 8px;
  width: 9px;
  background-size: contain;
  display: inline-block;
  position: relative;
  top: 1px;
  background-repeat: no-repeat;
  background-position: center;
}
.toggle-children-placeholder {
  display: inline-block;
  height: 10px;
  width: 10px;
  position: relative;
  top: 1px;
  padding-right: 3px;
}
.node-content-wrapper {
  display: inline-block;
  padding: 2px 5px;
  border-radius: 2px;
  transition: background-color .15s,box-shadow .15s;
}
.node-wrapper {display: flex; align-items: flex-start;}
.node-content-wrapper-active,
.node-content-wrapper.node-content-wrapper-active:hover,
.node-content-wrapper-active.node-content-wrapper-focused {
  background: #beebff;
}
.node-content-wrapper-focused { background: #e7f4f9 }
.node-content-wrapper:hover { background: #f7fbff }
.node-content-wrapper-active, .node-content-wrapper-focused, .node-content-wrapper:hover {
  box-shadow: inset 0 0 1px #999;
}
.node-content-wrapper.is-dragging-over { background: #ddffee; box-shadow: inset 0 0 1px #999; }
.node-content-wrapper.is-dragging-over-disabled { opacity: 0.5 }

tree-viewport {
  height: 100%;
  overflow: auto;
  display: block;
}
.tree-children { padding-left: 20px }
.empty-tree-drop-slot .node-drop-slot { height: 20px; min-width: 100px }
.angular-tree-component {
  width: 100%;
  position:relative;
  display: inline-block;
  cursor: pointer;
  -webkit-touch-callout: none; /* iOS Safari */
  -webkit-user-select: none;   /* Chrome/Safari/Opera */
  -khtml-user-select: none;    /* Konqueror */
  -moz-user-select: none;      /* Firefox */
  -ms-user-select: none;       /* IE/Edge */
  user-select: none;           /* non-prefixed version, currently not supported by any browser */
}

tree-root .angular-tree-component-rtl {
  direction: rtl;
}
tree-root .angular-tree-component-rtl .toggle-children-wrapper-collapsed .toggle-children {
  transform: rotate(180deg) !important;
}
tree-root .angular-tree-component-rtl .tree-children {
  padding-right: 20px;
  padding-left: 0;
}

tree-node-checkbox {
  padding: 1px;
}
    `
  ],
  templateUrl: './TreeviewComponent.html',
  encapsulation: ViewEncapsulation.None
})

export class TreeviewComponent {
  @Input() public nodes: Object = [{}];  
  @Input() public options: ITreeOptions = {};
  
  @Output() private onInitialized = new EventEmitter<any>();
  @Output() private onToggleExpanded = new EventEmitter<any>();
  @Output() private onActivate = new EventEmitter<any>();
  @Output() private onDeactivate = new EventEmitter<any>();
  @Output() private onFocus = new EventEmitter<any>();
  @Output() private onBlur = new EventEmitter<any>();
  
  public focused: boolean = true;
  public propagateChange: Function = null;

  constructor(private elRef: ElementRef, private renderer: Renderer2) {
  }
  
  get treeview(): any {
    return this.nodes;
  }

  set treeview(value: any) {
    this.nodes = value;
    if (this.propagateChange) {
        this.propagateChange(this.treeview);
    }
  }

  public onInitializedEvent($event) {
      console.log($event.eventName);
      this.onInitialized.emit($event);
  }

  public onToggleExpandedEvent($event) {
      console.log($event.eventName + " " + $event.node.data.id);
      this.onToggleExpanded.emit($event);
  }

  public onActivateEvent($event) {
      console.log($event.eventName + " " + $event.node.data.id);
      this.onActivate.emit($event);
  }
  
  public onDeactivateEvent($event) {
      console.log($event.eventName + " " + $event.node.data.id);
      this.onDeactivate.emit($event);
  }
  
  public onFocusEvent($event) {
      console.log($event.eventName + " " + $event.node.data.id);
      this.onFocus.emit($event);
  }
  
  public writeValue(value: any): void {
    this.nodes = value;
  }

  public registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  public ngAfterViewInit(): void {
  }
}
