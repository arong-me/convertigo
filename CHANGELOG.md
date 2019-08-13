# Changelog

## 7.6.3

#### Improvements:

- * [#178](https://github.com/convertigo/convertigo/issues/178) New 'Application' property "Use click for tap" to differently handle "(tap)" and "onTap" events
- * [#165](https://github.com/convertigo/convertigo/issues/165) Right-clicking a "SharedComponent" component from another project in Mobile App Viewer now focus element in treeview

#### Bug Fixes:

- [#179](https://github.com/convertigo/convertigo/issues/179) Fixed, UrlMapper is correctly exported with the project from the Server Administration console
- [#180](https://github.com/convertigo/convertigo/issues/180) Fixed, retry FullSync insertion when an IllegalStateException occurs, using a new HttpClient

---

## 7.6.2

#### Improvements:

- [#112](https://github.com/convertigo/convertigo/issues/112) In Studio, GIT actions that modify the project source asks user for refresh project
- [#174](https://github.com/convertigo/convertigo/issues/174) In Studio, Project menu have now 'GitFlow', 'Compare' and 'Replace' entries
- [#175](https://github.com/convertigo/convertigo/issues/175) In Studio, startup time divided by 4
- [#176](https://github.com/convertigo/convertigo/issues/176) MobileBuilder, improve application performances

#### Bug Fixes:
 
- [#164](https://github.com/convertigo/convertigo/issues/164) Fixed, MobileBuilder Sliding Tabs compile and can by used
- [#167](https://github.com/convertigo/convertigo/issues/167) Fixed, prevents deadlocks on calls for interdependent projects
- [#170](https://github.com/convertigo/convertigo/issues/170) Fixed, MobileBuilder RootPage action can now pass data to rooted page
- [#171](https://github.com/convertigo/convertigo/issues/171) Fixed, the javascript "use" function is now available instead of throwing a "ClassCastException"
- [#173](https://github.com/convertigo/convertigo/issues/173) Fixed, init admin password with Docker variable supported again

---

## 7.6.1

#### Bug Fixes:

- [#17](https://github.com/convertigo/convertigo/issues/17) Fixed, MobileBuilder Treeview component now compiles
- [#138](https://github.com/convertigo/convertigo/issues/138) Fixed, no more pop-up alert after closing a JS Editor in the Studio
- [#144](https://github.com/convertigo/convertigo/issues/144) Fixed, MobileBuilder can use older MB projects templates without compilation failures
- [#145](https://github.com/convertigo/convertigo/issues/145) Fixed, MobileBuilder Camera action works in the App Viewer
- [#148](https://github.com/convertigo/convertigo/issues/148) Fixed, no more collapsed treeview when adding / removing a component to a project
- [#149](https://github.com/convertigo/convertigo/issues/149) Fixed, no more freeze when using the SourcePicker
- [#152](https://github.com/convertigo/convertigo/issues/152) Fixed, can rename tasks in Scheduler's Administration widget
- [#156](https://github.com/convertigo/convertigo/issues/156) Fixed, prevent infinite loop of Scheduler execution, Jobs Group dependencies cannot be circular 
- [#157](https://github.com/convertigo/convertigo/issues/157) Fixed, Mobile Builder demos now build with their respective templates
- [#159](https://github.com/convertigo/convertigo/issues/159) Fixed, MobileBuilder build can fail if project folder name is different of the projet name
- [#160](https://github.com/convertigo/convertigo/issues/160) Fixed, the onSessionLost event subscription is now correctly generated in app.component.ts
- [#161](https://github.com/convertigo/convertigo/issues/161) Fixed, HttpTransaction using SSL for an untrusted certificate will work over a Squid proxy
- [#163](https://github.com/convertigo/convertigo/issues/163) Fixed, AnimateAction is now working again and can be placed inside a Shared Component

---

## 7.6.0

#### New Features:

- [#3](https://github.com/convertigo/convertigo/issues/3) UrlMapper: use swagger 3 and handle oas2 / oas3
- [#8](https://github.com/convertigo/convertigo/issues/8) Make Convertigo project more VCS friendly, use a shrinked yaml project descriptor split in several files
- [#9](https://github.com/convertigo/convertigo/issues/9) Integrate PDFBox jar file and create a PDF form step
- [#13](https://github.com/convertigo/convertigo/issues/13) Add a Mobile Builder Tooltip component
- [#22](https://github.com/convertigo/convertigo/issues/22) Implement a Database revision number for FullSync that allows to reset clients FS base automatically
- [#42](https://github.com/convertigo/convertigo/issues/42) Make a dark theme for Convertigo Studio
- [#54](https://github.com/convertigo/convertigo/issues/54) Studio: remove unwanted menu and toolbar item for Convertigo perspective
- [#55](https://github.com/convertigo/convertigo/issues/55) Better security for CouchDB to restrict access for the admin user
- [#60](https://github.com/convertigo/convertigo/issues/60) Add a Mobile Builder Ionic Infinite component
- [#72](https://github.com/convertigo/convertigo/issues/72) Add a Mobile Builder ClearDataSource action
- [#73](https://github.com/convertigo/convertigo/issues/73) Authenticated session responses now contains a X-Convertigo-Authenticated header (used by SDK)
- [#74](https://github.com/convertigo/convertigo/issues/74) Add support of Mobile Builder application shared actions
- [#76](https://github.com/convertigo/convertigo/issues/76) Introduce use, context.server.set/get and context.project.set/get to JS scope to cache Java method loading and share memory across sessions
- [#78](https://github.com/convertigo/convertigo/issues/78) Add a server and a project classloader directory for extra libraries
- [#88](https://github.com/convertigo/convertigo/issues/88) Add a CouchDB/FullSync AllDocs and Purge transactions
- [#95](https://github.com/convertigo/convertigo/issues/95) Add support of Mobile Builder App events
- [#101](https://github.com/convertigo/convertigo/issues/101) Add a Mobile Builder IterateAction action
- [#105](https://github.com/convertigo/convertigo/issues/105) Ctrl+DND a Sequence in a Mobile Builder action creates the CallSequence component
- [#106](https://github.com/convertigo/convertigo/issues/106) Ctrl+DND a FullSync View in a Mobile Builder action creates the getView component
- [#111](https://github.com/convertigo/convertigo/issues/111) Add support of Mobile Builder App Shared Components
- [#124](https://github.com/convertigo/convertigo/issues/124) Ctrl+DND a SharedAction on an event or action creates the InvokeAction
- [#125](https://github.com/convertigo/convertigo/issues/125) Ctrl+DND a SharedComponent on a component creates the UseShared component
- [#127](https://github.com/convertigo/convertigo/issues/127) Add a Mobile Builder ClosePopover action
- [#137](https://github.com/convertigo/convertigo/issues/137) Add a new IfElseAction working with a ElseHandler
- [#141](https://github.com/convertigo/convertigo/issues/141) Add the lib_AmazonLEX in the new project wizard

#### Improvements:

- [#1](https://github.com/convertigo/convertigo/issues/1) Studio: Import and use projects from any folder (projects can be imported from a git repository elsewhere on the disk)
- [#19](https://github.com/convertigo/convertigo/issues/19) Update JxBrowser to 6.23.1 and use the 64bit version for Windows
- [#27](https://github.com/convertigo/convertigo/issues/27) Improve Database cache manager for Oracle XMLTYPE/CLOB handling
- [#33](https://github.com/convertigo/convertigo/issues/33) New Engine property to configure the current server Endpoint URL
- [#35](https://github.com/convertigo/convertigo/issues/35) Prevent Swagger console errors to popup in server mode
- [#44](https://github.com/convertigo/convertigo/issues/44) Do not initialize connector for 'void' transaction
- [#46](https://github.com/convertigo/convertigo/issues/46) UrlMapper: Basic authentication is improved by checking is user is already connected
- [#66](https://github.com/convertigo/convertigo/issues/66) Add a Mobile Builder Chooser action that both support iOS and Android
- [#67](https://github.com/convertigo/convertigo/issues/67) Add a Mobile Builder Progress Bar component
- [#71](https://github.com/convertigo/convertigo/issues/71) Mobile Builder SetGlobal actions are now displayed according to 'Property' and 'value' properties
- [#77](https://github.com/convertigo/convertigo/issues/77) JS 'log' object accepts log4j levels methods (fatal, info, warn, trace)
- [#81](https://github.com/convertigo/convertigo/issues/81) Enhance support for wkWebview for iOS for better performances
- [#86](https://github.com/convertigo/convertigo/issues/86) Improve Mobile Builder page generation speed
- [#87](https://github.com/convertigo/convertigo/issues/87) Sort mobile app template projects by descending order
- [#89](https://github.com/convertigo/convertigo/issues/89) Modal Action bean must have a parameter to suspend while displayed
- [#94](https://github.com/convertigo/convertigo/issues/94) HttpConnector: Use the standard SSL stack if there is no specific certificate for a Project
- [#96](https://github.com/convertigo/convertigo/issues/96) Convertigo SDK settings are now customizable
- [#102](https://github.com/convertigo/convertigo/issues/102) Re-use compiled expression for RhinoJS to reduce JVM classes memory space and have better performance
- [#104](https://github.com/convertigo/convertigo/issues/104) Scheduler now handles order and limited number of parallel job
- [#109](https://github.com/convertigo/convertigo/issues/109) Mobile component help in Reference Manual is better rendered
- [#110](https://github.com/convertigo/convertigo/issues/110) Mobile Builder Sequence Calls Actions loading spinner can now be disabled, and 2 new actions has been added to show and close loading spinners
- [#133](https://github.com/convertigo/convertigo/issues/133) Improve MB ForEach bean, now item and index elements be customized so they can be inserted in nested loops.
- [#134](https://github.com/convertigo/convertigo/issues/134) Handle TenantID in OAuth action for Azure
- [#140](https://github.com/convertigo/convertigo/issues/140) Enable GZip compression for text response by default

#### Bug Fixes:

- [#18](https://github.com/convertigo/convertigo/issues/18) Fixed the double save dialog on Studio closing without saving
- [#24](https://github.com/convertigo/convertigo/issues/24) Fixed the Call Step generates invalid call data when a variable sources a self closing tag
- [#26](https://github.com/convertigo/convertigo/issues/26) Fixed Mobile Builder CalendarPickerAction to work as expected
- [#32](https://github.com/convertigo/convertigo/issues/32) Fixed invalid XML generated with some unicode characters
- [#38](https://github.com/convertigo/convertigo/issues/38) Fixed extra blank lines added in saved content of editable mobile components
- [#39](https://github.com/convertigo/convertigo/issues/39) Fixed extra markers added in saved TS content of editable mobile components
- [#41](https://github.com/convertigo/convertigo/issues/41) Fixed the Mobile Palette that does not show all components
- [#43](https://github.com/convertigo/convertigo/issues/43) Fixed JDBC Oracle poor performances
- [#47](https://github.com/convertigo/convertigo/issues/47) Fixed UrlMapper: context/session not always removed
- [#48](https://github.com/convertigo/convertigo/issues/48) Fixed Sequencer: attributes generated through a 'copyOf' step are not appended to DOM
- [#53](https://github.com/convertigo/convertigo/issues/53) Fixed the administration project list displays wrong deployment date after export
- [#57](https://github.com/convertigo/convertigo/issues/57) Fixed Mobile Builder: some projects do not load due to a validator considered as invalid
- [#59](https://github.com/convertigo/convertigo/issues/59) Fixed Mobile Builder component drop that needs to install dependencies is not triggering the reinstall of dependencies
- [#61](https://github.com/convertigo/convertigo/issues/61) Fixed the greyed Next button when you select a new Fullsync Listener
- [#62](https://github.com/convertigo/convertigo/issues/62) Fixed typo of some bean documentation
- [#65](https://github.com/convertigo/convertigo/issues/65) Fixed the random disappearing of files from DisplayObjects/mobile/assets/i18n
- [#68](https://github.com/convertigo/convertigo/issues/68) Fixed barcodescanner plugin build for Android
- [#69](https://github.com/convertigo/convertigo/issues/69) Fixed all Mobile palette documentation links now to ionic3 documentation
- [#75](https://github.com/convertigo/convertigo/issues/75) Fixed ‘Null’ error when deleting a project
- [#80](https://github.com/convertigo/convertigo/issues/80) Fixed the popup error when no bean found with the search bar
- [#85](https://github.com/convertigo/convertigo/issues/85) Fixed the Invalid Thread Exception when adding a Component that requiring additional packages
- [#103](https://github.com/convertigo/convertigo/issues/103) Fixed Scheduler to count session and clear contexts
- [#114](https://github.com/convertigo/convertigo/issues/114) Fixed Certificate Mappings configuration in administration
- [#116](https://github.com/convertigo/convertigo/issues/116) Fixed NPE in the Studio stdout console when selecting the LogView
- [#126](https://github.com/convertigo/convertigo/issues/126) Fixed "heap out of memory" for some Mobile Builder builds in production mode
- [#128](https://github.com/convertigo/convertigo/issues/128) Fixed the transpilation failure for empty value in TS mode of Mobile Builder actions
- [#129](https://github.com/convertigo/convertigo/issues/129) Fixed Mobile Builder missing rebuilds 
- [#130](https://github.com/convertigo/convertigo/issues/130) Fixed Http response always in UTF-8 even if the Requestable defines an another Charset

## [pre 7.6.0 versions changelog](CHANGELOG.pre.7-6-0.md)
