/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars*/
"use strict";

var $protobuf = require("protobufjs/minimal");

// Common aliases
var $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
var $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

$root.appFrameProtoIn = (function() {

    /**
     * Namespace appFrameProtoIn.
     * @exports appFrameProtoIn
     * @namespace
     */
    var appFrameProtoIn = {};

    appFrameProtoIn.AppFrameMsgInProto = (function() {

        /**
         * Properties of an AppFrameMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IAppFrameMsgInProto
         * @property {Array.<appFrameProtoIn.IInputEventMsgInProto>|null} [events] AppFrameMsgInProto events
         * @property {appFrameProtoIn.IPasteEventMsgInProto|null} [paste] AppFrameMsgInProto paste
         * @property {appFrameProtoIn.ICopyEventMsgInProto|null} [copy] AppFrameMsgInProto copy
         * @property {appFrameProtoIn.IUploadEventMsgInProto|null} [upload] AppFrameMsgInProto upload
         * @property {appFrameProtoIn.IFilesSelectedEventMsgInProto|null} [selected] AppFrameMsgInProto selected
         * @property {appFrameProtoIn.IJsResultMsgInProto|null} [jsResponse] AppFrameMsgInProto jsResponse
         * @property {appFrameProtoIn.IJavaEvalRequestMsgInProto|null} [javaRequest] AppFrameMsgInProto javaRequest
         * @property {appFrameProtoIn.IPixelsAreaResponseMsgInProto|null} [pixelsResponse] AppFrameMsgInProto pixelsResponse
         * @property {appFrameProtoIn.IWindowEventMsgInProto|null} [window] AppFrameMsgInProto window
         * @property {appFrameProtoIn.IActionEventMsgInProto|null} [action] AppFrameMsgInProto action
         * @property {appFrameProtoIn.IAudioEventMsgInProto|null} [audio] AppFrameMsgInProto audio
         */

        /**
         * Constructs a new AppFrameMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents an AppFrameMsgInProto.
         * @implements IAppFrameMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IAppFrameMsgInProto=} [properties] Properties to set
         */
        function AppFrameMsgInProto(properties) {
            this.events = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * AppFrameMsgInProto events.
         * @member {Array.<appFrameProtoIn.IInputEventMsgInProto>} events
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.events = $util.emptyArray;

        /**
         * AppFrameMsgInProto paste.
         * @member {appFrameProtoIn.IPasteEventMsgInProto|null|undefined} paste
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.paste = null;

        /**
         * AppFrameMsgInProto copy.
         * @member {appFrameProtoIn.ICopyEventMsgInProto|null|undefined} copy
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.copy = null;

        /**
         * AppFrameMsgInProto upload.
         * @member {appFrameProtoIn.IUploadEventMsgInProto|null|undefined} upload
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.upload = null;

        /**
         * AppFrameMsgInProto selected.
         * @member {appFrameProtoIn.IFilesSelectedEventMsgInProto|null|undefined} selected
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.selected = null;

        /**
         * AppFrameMsgInProto jsResponse.
         * @member {appFrameProtoIn.IJsResultMsgInProto|null|undefined} jsResponse
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.jsResponse = null;

        /**
         * AppFrameMsgInProto javaRequest.
         * @member {appFrameProtoIn.IJavaEvalRequestMsgInProto|null|undefined} javaRequest
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.javaRequest = null;

        /**
         * AppFrameMsgInProto pixelsResponse.
         * @member {appFrameProtoIn.IPixelsAreaResponseMsgInProto|null|undefined} pixelsResponse
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.pixelsResponse = null;

        /**
         * AppFrameMsgInProto window.
         * @member {appFrameProtoIn.IWindowEventMsgInProto|null|undefined} window
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.window = null;

        /**
         * AppFrameMsgInProto action.
         * @member {appFrameProtoIn.IActionEventMsgInProto|null|undefined} action
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.action = null;

        /**
         * AppFrameMsgInProto audio.
         * @member {appFrameProtoIn.IAudioEventMsgInProto|null|undefined} audio
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         */
        AppFrameMsgInProto.prototype.audio = null;

        /**
         * Creates a new AppFrameMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @static
         * @param {appFrameProtoIn.IAppFrameMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.AppFrameMsgInProto} AppFrameMsgInProto instance
         */
        AppFrameMsgInProto.create = function create(properties) {
            return new AppFrameMsgInProto(properties);
        };

        /**
         * Encodes the specified AppFrameMsgInProto message. Does not implicitly {@link appFrameProtoIn.AppFrameMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @static
         * @param {appFrameProtoIn.IAppFrameMsgInProto} message AppFrameMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AppFrameMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.events != null && message.events.length)
                for (var i = 0; i < message.events.length; ++i)
                    $root.appFrameProtoIn.InputEventMsgInProto.encode(message.events[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            if (message.paste != null && message.hasOwnProperty("paste"))
                $root.appFrameProtoIn.PasteEventMsgInProto.encode(message.paste, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.copy != null && message.hasOwnProperty("copy"))
                $root.appFrameProtoIn.CopyEventMsgInProto.encode(message.copy, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            if (message.upload != null && message.hasOwnProperty("upload"))
                $root.appFrameProtoIn.UploadEventMsgInProto.encode(message.upload, writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            if (message.selected != null && message.hasOwnProperty("selected"))
                $root.appFrameProtoIn.FilesSelectedEventMsgInProto.encode(message.selected, writer.uint32(/* id 5, wireType 2 =*/42).fork()).ldelim();
            if (message.jsResponse != null && message.hasOwnProperty("jsResponse"))
                $root.appFrameProtoIn.JsResultMsgInProto.encode(message.jsResponse, writer.uint32(/* id 6, wireType 2 =*/50).fork()).ldelim();
            if (message.javaRequest != null && message.hasOwnProperty("javaRequest"))
                $root.appFrameProtoIn.JavaEvalRequestMsgInProto.encode(message.javaRequest, writer.uint32(/* id 7, wireType 2 =*/58).fork()).ldelim();
            if (message.pixelsResponse != null && message.hasOwnProperty("pixelsResponse"))
                $root.appFrameProtoIn.PixelsAreaResponseMsgInProto.encode(message.pixelsResponse, writer.uint32(/* id 8, wireType 2 =*/66).fork()).ldelim();
            if (message.window != null && message.hasOwnProperty("window"))
                $root.appFrameProtoIn.WindowEventMsgInProto.encode(message.window, writer.uint32(/* id 9, wireType 2 =*/74).fork()).ldelim();
            if (message.action != null && message.hasOwnProperty("action"))
                $root.appFrameProtoIn.ActionEventMsgInProto.encode(message.action, writer.uint32(/* id 10, wireType 2 =*/82).fork()).ldelim();
            if (message.audio != null && message.hasOwnProperty("audio"))
                $root.appFrameProtoIn.AudioEventMsgInProto.encode(message.audio, writer.uint32(/* id 11, wireType 2 =*/90).fork()).ldelim();
            return writer;
        };

        /**
         * Creates an AppFrameMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.AppFrameMsgInProto} AppFrameMsgInProto
         */
        AppFrameMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.AppFrameMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.AppFrameMsgInProto();
            if (object.events) {
                if (!Array.isArray(object.events))
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.events: array expected");
                message.events = [];
                for (var i = 0; i < object.events.length; ++i) {
                    if (typeof object.events[i] !== "object")
                        throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.events: object expected");
                    message.events[i] = $root.appFrameProtoIn.InputEventMsgInProto.fromObject(object.events[i]);
                }
            }
            if (object.paste != null) {
                if (typeof object.paste !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.paste: object expected");
                message.paste = $root.appFrameProtoIn.PasteEventMsgInProto.fromObject(object.paste);
            }
            if (object.copy != null) {
                if (typeof object.copy !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.copy: object expected");
                message.copy = $root.appFrameProtoIn.CopyEventMsgInProto.fromObject(object.copy);
            }
            if (object.upload != null) {
                if (typeof object.upload !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.upload: object expected");
                message.upload = $root.appFrameProtoIn.UploadEventMsgInProto.fromObject(object.upload);
            }
            if (object.selected != null) {
                if (typeof object.selected !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.selected: object expected");
                message.selected = $root.appFrameProtoIn.FilesSelectedEventMsgInProto.fromObject(object.selected);
            }
            if (object.jsResponse != null) {
                if (typeof object.jsResponse !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.jsResponse: object expected");
                message.jsResponse = $root.appFrameProtoIn.JsResultMsgInProto.fromObject(object.jsResponse);
            }
            if (object.javaRequest != null) {
                if (typeof object.javaRequest !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.javaRequest: object expected");
                message.javaRequest = $root.appFrameProtoIn.JavaEvalRequestMsgInProto.fromObject(object.javaRequest);
            }
            if (object.pixelsResponse != null) {
                if (typeof object.pixelsResponse !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.pixelsResponse: object expected");
                message.pixelsResponse = $root.appFrameProtoIn.PixelsAreaResponseMsgInProto.fromObject(object.pixelsResponse);
            }
            if (object.window != null) {
                if (typeof object.window !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.window: object expected");
                message.window = $root.appFrameProtoIn.WindowEventMsgInProto.fromObject(object.window);
            }
            if (object.action != null) {
                if (typeof object.action !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.action: object expected");
                message.action = $root.appFrameProtoIn.ActionEventMsgInProto.fromObject(object.action);
            }
            if (object.audio != null) {
                if (typeof object.audio !== "object")
                    throw TypeError(".appFrameProtoIn.AppFrameMsgInProto.audio: object expected");
                message.audio = $root.appFrameProtoIn.AudioEventMsgInProto.fromObject(object.audio);
            }
            return message;
        };

        /**
         * Creates a plain object from an AppFrameMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @static
         * @param {appFrameProtoIn.AppFrameMsgInProto} message AppFrameMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        AppFrameMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.events = [];
            if (options.defaults) {
                object.paste = null;
                object.copy = null;
                object.upload = null;
                object.selected = null;
                object.jsResponse = null;
                object.javaRequest = null;
                object.pixelsResponse = null;
                object.window = null;
                object.action = null;
                object.audio = null;
            }
            if (message.events && message.events.length) {
                object.events = [];
                for (var j = 0; j < message.events.length; ++j)
                    object.events[j] = $root.appFrameProtoIn.InputEventMsgInProto.toObject(message.events[j], options);
            }
            if (message.paste != null && message.hasOwnProperty("paste"))
                object.paste = $root.appFrameProtoIn.PasteEventMsgInProto.toObject(message.paste, options);
            if (message.copy != null && message.hasOwnProperty("copy"))
                object.copy = $root.appFrameProtoIn.CopyEventMsgInProto.toObject(message.copy, options);
            if (message.upload != null && message.hasOwnProperty("upload"))
                object.upload = $root.appFrameProtoIn.UploadEventMsgInProto.toObject(message.upload, options);
            if (message.selected != null && message.hasOwnProperty("selected"))
                object.selected = $root.appFrameProtoIn.FilesSelectedEventMsgInProto.toObject(message.selected, options);
            if (message.jsResponse != null && message.hasOwnProperty("jsResponse"))
                object.jsResponse = $root.appFrameProtoIn.JsResultMsgInProto.toObject(message.jsResponse, options);
            if (message.javaRequest != null && message.hasOwnProperty("javaRequest"))
                object.javaRequest = $root.appFrameProtoIn.JavaEvalRequestMsgInProto.toObject(message.javaRequest, options);
            if (message.pixelsResponse != null && message.hasOwnProperty("pixelsResponse"))
                object.pixelsResponse = $root.appFrameProtoIn.PixelsAreaResponseMsgInProto.toObject(message.pixelsResponse, options);
            if (message.window != null && message.hasOwnProperty("window"))
                object.window = $root.appFrameProtoIn.WindowEventMsgInProto.toObject(message.window, options);
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = $root.appFrameProtoIn.ActionEventMsgInProto.toObject(message.action, options);
            if (message.audio != null && message.hasOwnProperty("audio"))
                object.audio = $root.appFrameProtoIn.AudioEventMsgInProto.toObject(message.audio, options);
            return object;
        };

        /**
         * Converts this AppFrameMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.AppFrameMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        AppFrameMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return AppFrameMsgInProto;
    })();

    appFrameProtoIn.InputEventMsgInProto = (function() {

        /**
         * Properties of an InputEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IInputEventMsgInProto
         * @property {appFrameProtoIn.IKeyboardEventMsgInProto|null} [key] InputEventMsgInProto key
         * @property {appFrameProtoIn.IMouseEventMsgInProto|null} [mouse] InputEventMsgInProto mouse
         * @property {appFrameProtoIn.IWindowFocusMsgInProto|null} [focus] InputEventMsgInProto focus
         */

        /**
         * Constructs a new InputEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents an InputEventMsgInProto.
         * @implements IInputEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IInputEventMsgInProto=} [properties] Properties to set
         */
        function InputEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * InputEventMsgInProto key.
         * @member {appFrameProtoIn.IKeyboardEventMsgInProto|null|undefined} key
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @instance
         */
        InputEventMsgInProto.prototype.key = null;

        /**
         * InputEventMsgInProto mouse.
         * @member {appFrameProtoIn.IMouseEventMsgInProto|null|undefined} mouse
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @instance
         */
        InputEventMsgInProto.prototype.mouse = null;

        /**
         * InputEventMsgInProto focus.
         * @member {appFrameProtoIn.IWindowFocusMsgInProto|null|undefined} focus
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @instance
         */
        InputEventMsgInProto.prototype.focus = null;

        /**
         * Creates a new InputEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IInputEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.InputEventMsgInProto} InputEventMsgInProto instance
         */
        InputEventMsgInProto.create = function create(properties) {
            return new InputEventMsgInProto(properties);
        };

        /**
         * Encodes the specified InputEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.InputEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IInputEventMsgInProto} message InputEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InputEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.key != null && message.hasOwnProperty("key"))
                $root.appFrameProtoIn.KeyboardEventMsgInProto.encode(message.key, writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            if (message.mouse != null && message.hasOwnProperty("mouse"))
                $root.appFrameProtoIn.MouseEventMsgInProto.encode(message.mouse, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.focus != null && message.hasOwnProperty("focus"))
                $root.appFrameProtoIn.WindowFocusMsgInProto.encode(message.focus, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            return writer;
        };

        /**
         * Creates an InputEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.InputEventMsgInProto} InputEventMsgInProto
         */
        InputEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.InputEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.InputEventMsgInProto();
            if (object.key != null) {
                if (typeof object.key !== "object")
                    throw TypeError(".appFrameProtoIn.InputEventMsgInProto.key: object expected");
                message.key = $root.appFrameProtoIn.KeyboardEventMsgInProto.fromObject(object.key);
            }
            if (object.mouse != null) {
                if (typeof object.mouse !== "object")
                    throw TypeError(".appFrameProtoIn.InputEventMsgInProto.mouse: object expected");
                message.mouse = $root.appFrameProtoIn.MouseEventMsgInProto.fromObject(object.mouse);
            }
            if (object.focus != null) {
                if (typeof object.focus !== "object")
                    throw TypeError(".appFrameProtoIn.InputEventMsgInProto.focus: object expected");
                message.focus = $root.appFrameProtoIn.WindowFocusMsgInProto.fromObject(object.focus);
            }
            return message;
        };

        /**
         * Creates a plain object from an InputEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @static
         * @param {appFrameProtoIn.InputEventMsgInProto} message InputEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        InputEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.key = null;
                object.mouse = null;
                object.focus = null;
            }
            if (message.key != null && message.hasOwnProperty("key"))
                object.key = $root.appFrameProtoIn.KeyboardEventMsgInProto.toObject(message.key, options);
            if (message.mouse != null && message.hasOwnProperty("mouse"))
                object.mouse = $root.appFrameProtoIn.MouseEventMsgInProto.toObject(message.mouse, options);
            if (message.focus != null && message.hasOwnProperty("focus"))
                object.focus = $root.appFrameProtoIn.WindowFocusMsgInProto.toObject(message.focus, options);
            return object;
        };

        /**
         * Converts this InputEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.InputEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        InputEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return InputEventMsgInProto;
    })();

    appFrameProtoIn.WindowFocusMsgInProto = (function() {

        /**
         * Properties of a WindowFocusMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IWindowFocusMsgInProto
         * @property {string|null} [windowId] WindowFocusMsgInProto windowId
         * @property {string|null} [htmlPanelId] WindowFocusMsgInProto htmlPanelId
         */

        /**
         * Constructs a new WindowFocusMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a WindowFocusMsgInProto.
         * @implements IWindowFocusMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IWindowFocusMsgInProto=} [properties] Properties to set
         */
        function WindowFocusMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * WindowFocusMsgInProto windowId.
         * @member {string} windowId
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @instance
         */
        WindowFocusMsgInProto.prototype.windowId = "";

        /**
         * WindowFocusMsgInProto htmlPanelId.
         * @member {string} htmlPanelId
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @instance
         */
        WindowFocusMsgInProto.prototype.htmlPanelId = "";

        /**
         * Creates a new WindowFocusMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @static
         * @param {appFrameProtoIn.IWindowFocusMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.WindowFocusMsgInProto} WindowFocusMsgInProto instance
         */
        WindowFocusMsgInProto.create = function create(properties) {
            return new WindowFocusMsgInProto(properties);
        };

        /**
         * Encodes the specified WindowFocusMsgInProto message. Does not implicitly {@link appFrameProtoIn.WindowFocusMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @static
         * @param {appFrameProtoIn.IWindowFocusMsgInProto} message WindowFocusMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        WindowFocusMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.windowId != null && message.hasOwnProperty("windowId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.windowId);
            if (message.htmlPanelId != null && message.hasOwnProperty("htmlPanelId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.htmlPanelId);
            return writer;
        };

        /**
         * Creates a WindowFocusMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.WindowFocusMsgInProto} WindowFocusMsgInProto
         */
        WindowFocusMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.WindowFocusMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.WindowFocusMsgInProto();
            if (object.windowId != null)
                message.windowId = String(object.windowId);
            if (object.htmlPanelId != null)
                message.htmlPanelId = String(object.htmlPanelId);
            return message;
        };

        /**
         * Creates a plain object from a WindowFocusMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @static
         * @param {appFrameProtoIn.WindowFocusMsgInProto} message WindowFocusMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        WindowFocusMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.windowId = "";
                object.htmlPanelId = "";
            }
            if (message.windowId != null && message.hasOwnProperty("windowId"))
                object.windowId = message.windowId;
            if (message.htmlPanelId != null && message.hasOwnProperty("htmlPanelId"))
                object.htmlPanelId = message.htmlPanelId;
            return object;
        };

        /**
         * Converts this WindowFocusMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.WindowFocusMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        WindowFocusMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return WindowFocusMsgInProto;
    })();

    appFrameProtoIn.KeyboardEventMsgInProto = (function() {

        /**
         * Properties of a KeyboardEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IKeyboardEventMsgInProto
         * @property {appFrameProtoIn.KeyboardEventMsgInProto.KeyEventTypeProto|null} [type] KeyboardEventMsgInProto type
         * @property {number|null} [character] KeyboardEventMsgInProto character
         * @property {number|null} [keycode] KeyboardEventMsgInProto keycode
         * @property {boolean|null} [alt] KeyboardEventMsgInProto alt
         * @property {boolean|null} [ctrl] KeyboardEventMsgInProto ctrl
         * @property {boolean|null} [shift] KeyboardEventMsgInProto shift
         * @property {boolean|null} [meta] KeyboardEventMsgInProto meta
         */

        /**
         * Constructs a new KeyboardEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a KeyboardEventMsgInProto.
         * @implements IKeyboardEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IKeyboardEventMsgInProto=} [properties] Properties to set
         */
        function KeyboardEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * KeyboardEventMsgInProto type.
         * @member {appFrameProtoIn.KeyboardEventMsgInProto.KeyEventTypeProto} type
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.type = 0;

        /**
         * KeyboardEventMsgInProto character.
         * @member {number} character
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.character = 0;

        /**
         * KeyboardEventMsgInProto keycode.
         * @member {number} keycode
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.keycode = 0;

        /**
         * KeyboardEventMsgInProto alt.
         * @member {boolean} alt
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.alt = false;

        /**
         * KeyboardEventMsgInProto ctrl.
         * @member {boolean} ctrl
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.ctrl = false;

        /**
         * KeyboardEventMsgInProto shift.
         * @member {boolean} shift
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.shift = false;

        /**
         * KeyboardEventMsgInProto meta.
         * @member {boolean} meta
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         */
        KeyboardEventMsgInProto.prototype.meta = false;

        /**
         * Creates a new KeyboardEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IKeyboardEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.KeyboardEventMsgInProto} KeyboardEventMsgInProto instance
         */
        KeyboardEventMsgInProto.create = function create(properties) {
            return new KeyboardEventMsgInProto(properties);
        };

        /**
         * Encodes the specified KeyboardEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.KeyboardEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IKeyboardEventMsgInProto} message KeyboardEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        KeyboardEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && message.hasOwnProperty("type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.character != null && message.hasOwnProperty("character"))
                writer.uint32(/* id 2, wireType 0 =*/16).sint32(message.character);
            if (message.keycode != null && message.hasOwnProperty("keycode"))
                writer.uint32(/* id 3, wireType 0 =*/24).sint32(message.keycode);
            if (message.alt != null && message.hasOwnProperty("alt"))
                writer.uint32(/* id 4, wireType 0 =*/32).bool(message.alt);
            if (message.ctrl != null && message.hasOwnProperty("ctrl"))
                writer.uint32(/* id 5, wireType 0 =*/40).bool(message.ctrl);
            if (message.shift != null && message.hasOwnProperty("shift"))
                writer.uint32(/* id 6, wireType 0 =*/48).bool(message.shift);
            if (message.meta != null && message.hasOwnProperty("meta"))
                writer.uint32(/* id 7, wireType 0 =*/56).bool(message.meta);
            return writer;
        };

        /**
         * Creates a KeyboardEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.KeyboardEventMsgInProto} KeyboardEventMsgInProto
         */
        KeyboardEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.KeyboardEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.KeyboardEventMsgInProto();
            switch (object.type) {
            case "keypress":
            case 0:
                message.type = 0;
                break;
            case "keydown":
            case 1:
                message.type = 1;
                break;
            case "keyup":
            case 2:
                message.type = 2;
                break;
            }
            if (object.character != null)
                message.character = object.character | 0;
            if (object.keycode != null)
                message.keycode = object.keycode | 0;
            if (object.alt != null)
                message.alt = Boolean(object.alt);
            if (object.ctrl != null)
                message.ctrl = Boolean(object.ctrl);
            if (object.shift != null)
                message.shift = Boolean(object.shift);
            if (object.meta != null)
                message.meta = Boolean(object.meta);
            return message;
        };

        /**
         * Creates a plain object from a KeyboardEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @static
         * @param {appFrameProtoIn.KeyboardEventMsgInProto} message KeyboardEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        KeyboardEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.type = options.enums === String ? "keypress" : 0;
                object.character = 0;
                object.keycode = 0;
                object.alt = false;
                object.ctrl = false;
                object.shift = false;
                object.meta = false;
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.appFrameProtoIn.KeyboardEventMsgInProto.KeyEventTypeProto[message.type] : message.type;
            if (message.character != null && message.hasOwnProperty("character"))
                object.character = message.character;
            if (message.keycode != null && message.hasOwnProperty("keycode"))
                object.keycode = message.keycode;
            if (message.alt != null && message.hasOwnProperty("alt"))
                object.alt = message.alt;
            if (message.ctrl != null && message.hasOwnProperty("ctrl"))
                object.ctrl = message.ctrl;
            if (message.shift != null && message.hasOwnProperty("shift"))
                object.shift = message.shift;
            if (message.meta != null && message.hasOwnProperty("meta"))
                object.meta = message.meta;
            return object;
        };

        /**
         * Converts this KeyboardEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.KeyboardEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        KeyboardEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * KeyEventTypeProto enum.
         * @name appFrameProtoIn.KeyboardEventMsgInProto.KeyEventTypeProto
         * @enum {string}
         * @property {number} keypress=0 keypress value
         * @property {number} keydown=1 keydown value
         * @property {number} keyup=2 keyup value
         */
        KeyboardEventMsgInProto.KeyEventTypeProto = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "keypress"] = 0;
            values[valuesById[1] = "keydown"] = 1;
            values[valuesById[2] = "keyup"] = 2;
            return values;
        })();

        return KeyboardEventMsgInProto;
    })();

    appFrameProtoIn.MouseEventMsgInProto = (function() {

        /**
         * Properties of a MouseEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IMouseEventMsgInProto
         * @property {appFrameProtoIn.MouseEventMsgInProto.MouseEventTypeProto|null} [type] MouseEventMsgInProto type
         * @property {number|null} [x] MouseEventMsgInProto x
         * @property {number|null} [y] MouseEventMsgInProto y
         * @property {number|null} [wheelDelta] MouseEventMsgInProto wheelDelta
         * @property {number|null} [button] MouseEventMsgInProto button
         * @property {boolean|null} [ctrl] MouseEventMsgInProto ctrl
         * @property {boolean|null} [alt] MouseEventMsgInProto alt
         * @property {boolean|null} [shift] MouseEventMsgInProto shift
         * @property {boolean|null} [meta] MouseEventMsgInProto meta
         * @property {number|null} [buttons] MouseEventMsgInProto buttons
         * @property {number|null} [timeMilis] MouseEventMsgInProto timeMilis
         * @property {string|null} [winId] MouseEventMsgInProto winId
         */

        /**
         * Constructs a new MouseEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a MouseEventMsgInProto.
         * @implements IMouseEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IMouseEventMsgInProto=} [properties] Properties to set
         */
        function MouseEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * MouseEventMsgInProto type.
         * @member {appFrameProtoIn.MouseEventMsgInProto.MouseEventTypeProto} type
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.type = 0;

        /**
         * MouseEventMsgInProto x.
         * @member {number} x
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.x = 0;

        /**
         * MouseEventMsgInProto y.
         * @member {number} y
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.y = 0;

        /**
         * MouseEventMsgInProto wheelDelta.
         * @member {number} wheelDelta
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.wheelDelta = 0;

        /**
         * MouseEventMsgInProto button.
         * @member {number} button
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.button = 0;

        /**
         * MouseEventMsgInProto ctrl.
         * @member {boolean} ctrl
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.ctrl = false;

        /**
         * MouseEventMsgInProto alt.
         * @member {boolean} alt
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.alt = false;

        /**
         * MouseEventMsgInProto shift.
         * @member {boolean} shift
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.shift = false;

        /**
         * MouseEventMsgInProto meta.
         * @member {boolean} meta
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.meta = false;

        /**
         * MouseEventMsgInProto buttons.
         * @member {number} buttons
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.buttons = 0;

        /**
         * MouseEventMsgInProto timeMilis.
         * @member {number} timeMilis
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.timeMilis = 0;

        /**
         * MouseEventMsgInProto winId.
         * @member {string} winId
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         */
        MouseEventMsgInProto.prototype.winId = "";

        /**
         * Creates a new MouseEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IMouseEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.MouseEventMsgInProto} MouseEventMsgInProto instance
         */
        MouseEventMsgInProto.create = function create(properties) {
            return new MouseEventMsgInProto(properties);
        };

        /**
         * Encodes the specified MouseEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.MouseEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IMouseEventMsgInProto} message MouseEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MouseEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && message.hasOwnProperty("type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.x != null && message.hasOwnProperty("x"))
                writer.uint32(/* id 2, wireType 0 =*/16).sint32(message.x);
            if (message.y != null && message.hasOwnProperty("y"))
                writer.uint32(/* id 3, wireType 0 =*/24).sint32(message.y);
            if (message.wheelDelta != null && message.hasOwnProperty("wheelDelta"))
                writer.uint32(/* id 4, wireType 0 =*/32).sint32(message.wheelDelta);
            if (message.button != null && message.hasOwnProperty("button"))
                writer.uint32(/* id 5, wireType 0 =*/40).sint32(message.button);
            if (message.ctrl != null && message.hasOwnProperty("ctrl"))
                writer.uint32(/* id 6, wireType 0 =*/48).bool(message.ctrl);
            if (message.alt != null && message.hasOwnProperty("alt"))
                writer.uint32(/* id 7, wireType 0 =*/56).bool(message.alt);
            if (message.shift != null && message.hasOwnProperty("shift"))
                writer.uint32(/* id 8, wireType 0 =*/64).bool(message.shift);
            if (message.meta != null && message.hasOwnProperty("meta"))
                writer.uint32(/* id 9, wireType 0 =*/72).bool(message.meta);
            if (message.buttons != null && message.hasOwnProperty("buttons"))
                writer.uint32(/* id 10, wireType 0 =*/80).sint32(message.buttons);
            if (message.timeMilis != null && message.hasOwnProperty("timeMilis"))
                writer.uint32(/* id 11, wireType 0 =*/88).sint32(message.timeMilis);
            if (message.winId != null && message.hasOwnProperty("winId"))
                writer.uint32(/* id 12, wireType 2 =*/98).string(message.winId);
            return writer;
        };

        /**
         * Creates a MouseEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.MouseEventMsgInProto} MouseEventMsgInProto
         */
        MouseEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.MouseEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.MouseEventMsgInProto();
            switch (object.type) {
            case "mousemove":
            case 0:
                message.type = 0;
                break;
            case "mousedown":
            case 1:
                message.type = 1;
                break;
            case "mouseup":
            case 2:
                message.type = 2;
                break;
            case "mousewheel":
            case 3:
                message.type = 3;
                break;
            case "dblclick":
            case 4:
                message.type = 4;
                break;
            }
            if (object.x != null)
                message.x = object.x | 0;
            if (object.y != null)
                message.y = object.y | 0;
            if (object.wheelDelta != null)
                message.wheelDelta = object.wheelDelta | 0;
            if (object.button != null)
                message.button = object.button | 0;
            if (object.ctrl != null)
                message.ctrl = Boolean(object.ctrl);
            if (object.alt != null)
                message.alt = Boolean(object.alt);
            if (object.shift != null)
                message.shift = Boolean(object.shift);
            if (object.meta != null)
                message.meta = Boolean(object.meta);
            if (object.buttons != null)
                message.buttons = object.buttons | 0;
            if (object.timeMilis != null)
                message.timeMilis = object.timeMilis | 0;
            if (object.winId != null)
                message.winId = String(object.winId);
            return message;
        };

        /**
         * Creates a plain object from a MouseEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @static
         * @param {appFrameProtoIn.MouseEventMsgInProto} message MouseEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        MouseEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.type = options.enums === String ? "mousemove" : 0;
                object.x = 0;
                object.y = 0;
                object.wheelDelta = 0;
                object.button = 0;
                object.ctrl = false;
                object.alt = false;
                object.shift = false;
                object.meta = false;
                object.buttons = 0;
                object.timeMilis = 0;
                object.winId = "";
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.appFrameProtoIn.MouseEventMsgInProto.MouseEventTypeProto[message.type] : message.type;
            if (message.x != null && message.hasOwnProperty("x"))
                object.x = message.x;
            if (message.y != null && message.hasOwnProperty("y"))
                object.y = message.y;
            if (message.wheelDelta != null && message.hasOwnProperty("wheelDelta"))
                object.wheelDelta = message.wheelDelta;
            if (message.button != null && message.hasOwnProperty("button"))
                object.button = message.button;
            if (message.ctrl != null && message.hasOwnProperty("ctrl"))
                object.ctrl = message.ctrl;
            if (message.alt != null && message.hasOwnProperty("alt"))
                object.alt = message.alt;
            if (message.shift != null && message.hasOwnProperty("shift"))
                object.shift = message.shift;
            if (message.meta != null && message.hasOwnProperty("meta"))
                object.meta = message.meta;
            if (message.buttons != null && message.hasOwnProperty("buttons"))
                object.buttons = message.buttons;
            if (message.timeMilis != null && message.hasOwnProperty("timeMilis"))
                object.timeMilis = message.timeMilis;
            if (message.winId != null && message.hasOwnProperty("winId"))
                object.winId = message.winId;
            return object;
        };

        /**
         * Converts this MouseEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.MouseEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        MouseEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * MouseEventTypeProto enum.
         * @name appFrameProtoIn.MouseEventMsgInProto.MouseEventTypeProto
         * @enum {string}
         * @property {number} mousemove=0 mousemove value
         * @property {number} mousedown=1 mousedown value
         * @property {number} mouseup=2 mouseup value
         * @property {number} mousewheel=3 mousewheel value
         * @property {number} dblclick=4 dblclick value
         */
        MouseEventMsgInProto.MouseEventTypeProto = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "mousemove"] = 0;
            values[valuesById[1] = "mousedown"] = 1;
            values[valuesById[2] = "mouseup"] = 2;
            values[valuesById[3] = "mousewheel"] = 3;
            values[valuesById[4] = "dblclick"] = 4;
            return values;
        })();

        return MouseEventMsgInProto;
    })();

    appFrameProtoIn.CopyEventMsgInProto = (function() {

        /**
         * Properties of a CopyEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface ICopyEventMsgInProto
         * @property {appFrameProtoIn.CopyEventMsgInProto.CopyEventMsgTypeProto|null} [type] CopyEventMsgInProto type
         * @property {string|null} [file] CopyEventMsgInProto file
         */

        /**
         * Constructs a new CopyEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a CopyEventMsgInProto.
         * @implements ICopyEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.ICopyEventMsgInProto=} [properties] Properties to set
         */
        function CopyEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * CopyEventMsgInProto type.
         * @member {appFrameProtoIn.CopyEventMsgInProto.CopyEventMsgTypeProto} type
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @instance
         */
        CopyEventMsgInProto.prototype.type = 0;

        /**
         * CopyEventMsgInProto file.
         * @member {string} file
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @instance
         */
        CopyEventMsgInProto.prototype.file = "";

        /**
         * Creates a new CopyEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @static
         * @param {appFrameProtoIn.ICopyEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.CopyEventMsgInProto} CopyEventMsgInProto instance
         */
        CopyEventMsgInProto.create = function create(properties) {
            return new CopyEventMsgInProto(properties);
        };

        /**
         * Encodes the specified CopyEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.CopyEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @static
         * @param {appFrameProtoIn.ICopyEventMsgInProto} message CopyEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        CopyEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && message.hasOwnProperty("type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.file != null && message.hasOwnProperty("file"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.file);
            return writer;
        };

        /**
         * Creates a CopyEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.CopyEventMsgInProto} CopyEventMsgInProto
         */
        CopyEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.CopyEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.CopyEventMsgInProto();
            switch (object.type) {
            case "copy":
            case 0:
                message.type = 0;
                break;
            case "cut":
            case 1:
                message.type = 1;
                break;
            case "getFileFromClipboard":
            case 2:
                message.type = 2;
                break;
            }
            if (object.file != null)
                message.file = String(object.file);
            return message;
        };

        /**
         * Creates a plain object from a CopyEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @static
         * @param {appFrameProtoIn.CopyEventMsgInProto} message CopyEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        CopyEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.type = options.enums === String ? "copy" : 0;
                object.file = "";
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.appFrameProtoIn.CopyEventMsgInProto.CopyEventMsgTypeProto[message.type] : message.type;
            if (message.file != null && message.hasOwnProperty("file"))
                object.file = message.file;
            return object;
        };

        /**
         * Converts this CopyEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.CopyEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        CopyEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * CopyEventMsgTypeProto enum.
         * @name appFrameProtoIn.CopyEventMsgInProto.CopyEventMsgTypeProto
         * @enum {string}
         * @property {number} copy=0 copy value
         * @property {number} cut=1 cut value
         * @property {number} getFileFromClipboard=2 getFileFromClipboard value
         */
        CopyEventMsgInProto.CopyEventMsgTypeProto = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "copy"] = 0;
            values[valuesById[1] = "cut"] = 1;
            values[valuesById[2] = "getFileFromClipboard"] = 2;
            return values;
        })();

        return CopyEventMsgInProto;
    })();

    appFrameProtoIn.PasteEventMsgInProto = (function() {

        /**
         * Properties of a PasteEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IPasteEventMsgInProto
         * @property {string|null} [text] PasteEventMsgInProto text
         * @property {string|null} [html] PasteEventMsgInProto html
         * @property {string|null} [img] PasteEventMsgInProto img
         * @property {boolean|null} [special] PasteEventMsgInProto special
         */

        /**
         * Constructs a new PasteEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a PasteEventMsgInProto.
         * @implements IPasteEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IPasteEventMsgInProto=} [properties] Properties to set
         */
        function PasteEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PasteEventMsgInProto text.
         * @member {string} text
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @instance
         */
        PasteEventMsgInProto.prototype.text = "";

        /**
         * PasteEventMsgInProto html.
         * @member {string} html
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @instance
         */
        PasteEventMsgInProto.prototype.html = "";

        /**
         * PasteEventMsgInProto img.
         * @member {string} img
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @instance
         */
        PasteEventMsgInProto.prototype.img = "";

        /**
         * PasteEventMsgInProto special.
         * @member {boolean} special
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @instance
         */
        PasteEventMsgInProto.prototype.special = false;

        /**
         * Creates a new PasteEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IPasteEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.PasteEventMsgInProto} PasteEventMsgInProto instance
         */
        PasteEventMsgInProto.create = function create(properties) {
            return new PasteEventMsgInProto(properties);
        };

        /**
         * Encodes the specified PasteEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.PasteEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IPasteEventMsgInProto} message PasteEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PasteEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.text != null && message.hasOwnProperty("text"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.text);
            if (message.html != null && message.hasOwnProperty("html"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.html);
            if (message.img != null && message.hasOwnProperty("img"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.img);
            if (message.special != null && message.hasOwnProperty("special"))
                writer.uint32(/* id 4, wireType 0 =*/32).bool(message.special);
            return writer;
        };

        /**
         * Creates a PasteEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.PasteEventMsgInProto} PasteEventMsgInProto
         */
        PasteEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.PasteEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.PasteEventMsgInProto();
            if (object.text != null)
                message.text = String(object.text);
            if (object.html != null)
                message.html = String(object.html);
            if (object.img != null)
                message.img = String(object.img);
            if (object.special != null)
                message.special = Boolean(object.special);
            return message;
        };

        /**
         * Creates a plain object from a PasteEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @static
         * @param {appFrameProtoIn.PasteEventMsgInProto} message PasteEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PasteEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.text = "";
                object.html = "";
                object.img = "";
                object.special = false;
            }
            if (message.text != null && message.hasOwnProperty("text"))
                object.text = message.text;
            if (message.html != null && message.hasOwnProperty("html"))
                object.html = message.html;
            if (message.img != null && message.hasOwnProperty("img"))
                object.img = message.img;
            if (message.special != null && message.hasOwnProperty("special"))
                object.special = message.special;
            return object;
        };

        /**
         * Converts this PasteEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.PasteEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PasteEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return PasteEventMsgInProto;
    })();

    appFrameProtoIn.FilesSelectedEventMsgInProto = (function() {

        /**
         * Properties of a FilesSelectedEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IFilesSelectedEventMsgInProto
         * @property {Array.<string>|null} [files] FilesSelectedEventMsgInProto files
         */

        /**
         * Constructs a new FilesSelectedEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a FilesSelectedEventMsgInProto.
         * @implements IFilesSelectedEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IFilesSelectedEventMsgInProto=} [properties] Properties to set
         */
        function FilesSelectedEventMsgInProto(properties) {
            this.files = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * FilesSelectedEventMsgInProto files.
         * @member {Array.<string>} files
         * @memberof appFrameProtoIn.FilesSelectedEventMsgInProto
         * @instance
         */
        FilesSelectedEventMsgInProto.prototype.files = $util.emptyArray;

        /**
         * Creates a new FilesSelectedEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.FilesSelectedEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IFilesSelectedEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.FilesSelectedEventMsgInProto} FilesSelectedEventMsgInProto instance
         */
        FilesSelectedEventMsgInProto.create = function create(properties) {
            return new FilesSelectedEventMsgInProto(properties);
        };

        /**
         * Encodes the specified FilesSelectedEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.FilesSelectedEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.FilesSelectedEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IFilesSelectedEventMsgInProto} message FilesSelectedEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        FilesSelectedEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.files != null && message.files.length)
                for (var i = 0; i < message.files.length; ++i)
                    writer.uint32(/* id 1, wireType 2 =*/10).string(message.files[i]);
            return writer;
        };

        /**
         * Creates a FilesSelectedEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.FilesSelectedEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.FilesSelectedEventMsgInProto} FilesSelectedEventMsgInProto
         */
        FilesSelectedEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.FilesSelectedEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.FilesSelectedEventMsgInProto();
            if (object.files) {
                if (!Array.isArray(object.files))
                    throw TypeError(".appFrameProtoIn.FilesSelectedEventMsgInProto.files: array expected");
                message.files = [];
                for (var i = 0; i < object.files.length; ++i)
                    message.files[i] = String(object.files[i]);
            }
            return message;
        };

        /**
         * Creates a plain object from a FilesSelectedEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.FilesSelectedEventMsgInProto
         * @static
         * @param {appFrameProtoIn.FilesSelectedEventMsgInProto} message FilesSelectedEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        FilesSelectedEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.files = [];
            if (message.files && message.files.length) {
                object.files = [];
                for (var j = 0; j < message.files.length; ++j)
                    object.files[j] = message.files[j];
            }
            return object;
        };

        /**
         * Converts this FilesSelectedEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.FilesSelectedEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        FilesSelectedEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return FilesSelectedEventMsgInProto;
    })();

    appFrameProtoIn.UploadEventMsgInProto = (function() {

        /**
         * Properties of an UploadEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IUploadEventMsgInProto
         * @property {string|null} [fileId] UploadEventMsgInProto fileId
         */

        /**
         * Constructs a new UploadEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents an UploadEventMsgInProto.
         * @implements IUploadEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IUploadEventMsgInProto=} [properties] Properties to set
         */
        function UploadEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * UploadEventMsgInProto fileId.
         * @member {string} fileId
         * @memberof appFrameProtoIn.UploadEventMsgInProto
         * @instance
         */
        UploadEventMsgInProto.prototype.fileId = "";

        /**
         * Creates a new UploadEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.UploadEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IUploadEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.UploadEventMsgInProto} UploadEventMsgInProto instance
         */
        UploadEventMsgInProto.create = function create(properties) {
            return new UploadEventMsgInProto(properties);
        };

        /**
         * Encodes the specified UploadEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.UploadEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.UploadEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IUploadEventMsgInProto} message UploadEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.fileId != null && message.hasOwnProperty("fileId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.fileId);
            return writer;
        };

        /**
         * Creates an UploadEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.UploadEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.UploadEventMsgInProto} UploadEventMsgInProto
         */
        UploadEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.UploadEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.UploadEventMsgInProto();
            if (object.fileId != null)
                message.fileId = String(object.fileId);
            return message;
        };

        /**
         * Creates a plain object from an UploadEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.UploadEventMsgInProto
         * @static
         * @param {appFrameProtoIn.UploadEventMsgInProto} message UploadEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        UploadEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.fileId = "";
            if (message.fileId != null && message.hasOwnProperty("fileId"))
                object.fileId = message.fileId;
            return object;
        };

        /**
         * Converts this UploadEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.UploadEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        UploadEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return UploadEventMsgInProto;
    })();

    appFrameProtoIn.JavaEvalRequestMsgInProto = (function() {

        /**
         * Properties of a JavaEvalRequestMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IJavaEvalRequestMsgInProto
         * @property {string|null} [correlationId] JavaEvalRequestMsgInProto correlationId
         * @property {string|null} [objectId] JavaEvalRequestMsgInProto objectId
         * @property {string|null} [method] JavaEvalRequestMsgInProto method
         * @property {Array.<appFrameProtoIn.IJsParamMsgInProto>|null} [params] JavaEvalRequestMsgInProto params
         */

        /**
         * Constructs a new JavaEvalRequestMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a JavaEvalRequestMsgInProto.
         * @implements IJavaEvalRequestMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IJavaEvalRequestMsgInProto=} [properties] Properties to set
         */
        function JavaEvalRequestMsgInProto(properties) {
            this.params = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * JavaEvalRequestMsgInProto correlationId.
         * @member {string} correlationId
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @instance
         */
        JavaEvalRequestMsgInProto.prototype.correlationId = "";

        /**
         * JavaEvalRequestMsgInProto objectId.
         * @member {string} objectId
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @instance
         */
        JavaEvalRequestMsgInProto.prototype.objectId = "";

        /**
         * JavaEvalRequestMsgInProto method.
         * @member {string} method
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @instance
         */
        JavaEvalRequestMsgInProto.prototype.method = "";

        /**
         * JavaEvalRequestMsgInProto params.
         * @member {Array.<appFrameProtoIn.IJsParamMsgInProto>} params
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @instance
         */
        JavaEvalRequestMsgInProto.prototype.params = $util.emptyArray;

        /**
         * Creates a new JavaEvalRequestMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @static
         * @param {appFrameProtoIn.IJavaEvalRequestMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.JavaEvalRequestMsgInProto} JavaEvalRequestMsgInProto instance
         */
        JavaEvalRequestMsgInProto.create = function create(properties) {
            return new JavaEvalRequestMsgInProto(properties);
        };

        /**
         * Encodes the specified JavaEvalRequestMsgInProto message. Does not implicitly {@link appFrameProtoIn.JavaEvalRequestMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @static
         * @param {appFrameProtoIn.IJavaEvalRequestMsgInProto} message JavaEvalRequestMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        JavaEvalRequestMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.correlationId != null && message.hasOwnProperty("correlationId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.correlationId);
            if (message.objectId != null && message.hasOwnProperty("objectId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.objectId);
            if (message.method != null && message.hasOwnProperty("method"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.method);
            if (message.params != null && message.params.length)
                for (var i = 0; i < message.params.length; ++i)
                    $root.appFrameProtoIn.JsParamMsgInProto.encode(message.params[i], writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            return writer;
        };

        /**
         * Creates a JavaEvalRequestMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.JavaEvalRequestMsgInProto} JavaEvalRequestMsgInProto
         */
        JavaEvalRequestMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.JavaEvalRequestMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.JavaEvalRequestMsgInProto();
            if (object.correlationId != null)
                message.correlationId = String(object.correlationId);
            if (object.objectId != null)
                message.objectId = String(object.objectId);
            if (object.method != null)
                message.method = String(object.method);
            if (object.params) {
                if (!Array.isArray(object.params))
                    throw TypeError(".appFrameProtoIn.JavaEvalRequestMsgInProto.params: array expected");
                message.params = [];
                for (var i = 0; i < object.params.length; ++i) {
                    if (typeof object.params[i] !== "object")
                        throw TypeError(".appFrameProtoIn.JavaEvalRequestMsgInProto.params: object expected");
                    message.params[i] = $root.appFrameProtoIn.JsParamMsgInProto.fromObject(object.params[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a JavaEvalRequestMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @static
         * @param {appFrameProtoIn.JavaEvalRequestMsgInProto} message JavaEvalRequestMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        JavaEvalRequestMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.params = [];
            if (options.defaults) {
                object.correlationId = "";
                object.objectId = "";
                object.method = "";
            }
            if (message.correlationId != null && message.hasOwnProperty("correlationId"))
                object.correlationId = message.correlationId;
            if (message.objectId != null && message.hasOwnProperty("objectId"))
                object.objectId = message.objectId;
            if (message.method != null && message.hasOwnProperty("method"))
                object.method = message.method;
            if (message.params && message.params.length) {
                object.params = [];
                for (var j = 0; j < message.params.length; ++j)
                    object.params[j] = $root.appFrameProtoIn.JsParamMsgInProto.toObject(message.params[j], options);
            }
            return object;
        };

        /**
         * Converts this JavaEvalRequestMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.JavaEvalRequestMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        JavaEvalRequestMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return JavaEvalRequestMsgInProto;
    })();

    appFrameProtoIn.JsResultMsgInProto = (function() {

        /**
         * Properties of a JsResultMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IJsResultMsgInProto
         * @property {string|null} [correlationId] JsResultMsgInProto correlationId
         * @property {string|null} [error] JsResultMsgInProto error
         * @property {appFrameProtoIn.IJsParamMsgInProto|null} [value] JsResultMsgInProto value
         */

        /**
         * Constructs a new JsResultMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a JsResultMsgInProto.
         * @implements IJsResultMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IJsResultMsgInProto=} [properties] Properties to set
         */
        function JsResultMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * JsResultMsgInProto correlationId.
         * @member {string} correlationId
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @instance
         */
        JsResultMsgInProto.prototype.correlationId = "";

        /**
         * JsResultMsgInProto error.
         * @member {string} error
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @instance
         */
        JsResultMsgInProto.prototype.error = "";

        /**
         * JsResultMsgInProto value.
         * @member {appFrameProtoIn.IJsParamMsgInProto|null|undefined} value
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @instance
         */
        JsResultMsgInProto.prototype.value = null;

        /**
         * Creates a new JsResultMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @static
         * @param {appFrameProtoIn.IJsResultMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.JsResultMsgInProto} JsResultMsgInProto instance
         */
        JsResultMsgInProto.create = function create(properties) {
            return new JsResultMsgInProto(properties);
        };

        /**
         * Encodes the specified JsResultMsgInProto message. Does not implicitly {@link appFrameProtoIn.JsResultMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @static
         * @param {appFrameProtoIn.IJsResultMsgInProto} message JsResultMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        JsResultMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.correlationId != null && message.hasOwnProperty("correlationId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.correlationId);
            if (message.error != null && message.hasOwnProperty("error"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.error);
            if (message.value != null && message.hasOwnProperty("value"))
                $root.appFrameProtoIn.JsParamMsgInProto.encode(message.value, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            return writer;
        };

        /**
         * Creates a JsResultMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.JsResultMsgInProto} JsResultMsgInProto
         */
        JsResultMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.JsResultMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.JsResultMsgInProto();
            if (object.correlationId != null)
                message.correlationId = String(object.correlationId);
            if (object.error != null)
                message.error = String(object.error);
            if (object.value != null) {
                if (typeof object.value !== "object")
                    throw TypeError(".appFrameProtoIn.JsResultMsgInProto.value: object expected");
                message.value = $root.appFrameProtoIn.JsParamMsgInProto.fromObject(object.value);
            }
            return message;
        };

        /**
         * Creates a plain object from a JsResultMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @static
         * @param {appFrameProtoIn.JsResultMsgInProto} message JsResultMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        JsResultMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.correlationId = "";
                object.error = "";
                object.value = null;
            }
            if (message.correlationId != null && message.hasOwnProperty("correlationId"))
                object.correlationId = message.correlationId;
            if (message.error != null && message.hasOwnProperty("error"))
                object.error = message.error;
            if (message.value != null && message.hasOwnProperty("value"))
                object.value = $root.appFrameProtoIn.JsParamMsgInProto.toObject(message.value, options);
            return object;
        };

        /**
         * Converts this JsResultMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.JsResultMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        JsResultMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return JsResultMsgInProto;
    })();

    appFrameProtoIn.JsParamMsgInProto = (function() {

        /**
         * Properties of a JsParamMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IJsParamMsgInProto
         * @property {string|null} [primitive] JsParamMsgInProto primitive
         * @property {appFrameProtoIn.IJSObjectMsgInProto|null} [jsObject] JsParamMsgInProto jsObject
         * @property {appFrameProtoIn.IJavaObjectRefMsgInProto|null} [javaObject] JsParamMsgInProto javaObject
         * @property {Array.<appFrameProtoIn.IJsParamMsgInProto>|null} [array] JsParamMsgInProto array
         */

        /**
         * Constructs a new JsParamMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a JsParamMsgInProto.
         * @implements IJsParamMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IJsParamMsgInProto=} [properties] Properties to set
         */
        function JsParamMsgInProto(properties) {
            this.array = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * JsParamMsgInProto primitive.
         * @member {string} primitive
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @instance
         */
        JsParamMsgInProto.prototype.primitive = "";

        /**
         * JsParamMsgInProto jsObject.
         * @member {appFrameProtoIn.IJSObjectMsgInProto|null|undefined} jsObject
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @instance
         */
        JsParamMsgInProto.prototype.jsObject = null;

        /**
         * JsParamMsgInProto javaObject.
         * @member {appFrameProtoIn.IJavaObjectRefMsgInProto|null|undefined} javaObject
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @instance
         */
        JsParamMsgInProto.prototype.javaObject = null;

        /**
         * JsParamMsgInProto array.
         * @member {Array.<appFrameProtoIn.IJsParamMsgInProto>} array
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @instance
         */
        JsParamMsgInProto.prototype.array = $util.emptyArray;

        /**
         * Creates a new JsParamMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @static
         * @param {appFrameProtoIn.IJsParamMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.JsParamMsgInProto} JsParamMsgInProto instance
         */
        JsParamMsgInProto.create = function create(properties) {
            return new JsParamMsgInProto(properties);
        };

        /**
         * Encodes the specified JsParamMsgInProto message. Does not implicitly {@link appFrameProtoIn.JsParamMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @static
         * @param {appFrameProtoIn.IJsParamMsgInProto} message JsParamMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        JsParamMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.primitive != null && message.hasOwnProperty("primitive"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.primitive);
            if (message.jsObject != null && message.hasOwnProperty("jsObject"))
                $root.appFrameProtoIn.JSObjectMsgInProto.encode(message.jsObject, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.javaObject != null && message.hasOwnProperty("javaObject"))
                $root.appFrameProtoIn.JavaObjectRefMsgInProto.encode(message.javaObject, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            if (message.array != null && message.array.length)
                for (var i = 0; i < message.array.length; ++i)
                    $root.appFrameProtoIn.JsParamMsgInProto.encode(message.array[i], writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            return writer;
        };

        /**
         * Creates a JsParamMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.JsParamMsgInProto} JsParamMsgInProto
         */
        JsParamMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.JsParamMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.JsParamMsgInProto();
            if (object.primitive != null)
                message.primitive = String(object.primitive);
            if (object.jsObject != null) {
                if (typeof object.jsObject !== "object")
                    throw TypeError(".appFrameProtoIn.JsParamMsgInProto.jsObject: object expected");
                message.jsObject = $root.appFrameProtoIn.JSObjectMsgInProto.fromObject(object.jsObject);
            }
            if (object.javaObject != null) {
                if (typeof object.javaObject !== "object")
                    throw TypeError(".appFrameProtoIn.JsParamMsgInProto.javaObject: object expected");
                message.javaObject = $root.appFrameProtoIn.JavaObjectRefMsgInProto.fromObject(object.javaObject);
            }
            if (object.array) {
                if (!Array.isArray(object.array))
                    throw TypeError(".appFrameProtoIn.JsParamMsgInProto.array: array expected");
                message.array = [];
                for (var i = 0; i < object.array.length; ++i) {
                    if (typeof object.array[i] !== "object")
                        throw TypeError(".appFrameProtoIn.JsParamMsgInProto.array: object expected");
                    message.array[i] = $root.appFrameProtoIn.JsParamMsgInProto.fromObject(object.array[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a JsParamMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @static
         * @param {appFrameProtoIn.JsParamMsgInProto} message JsParamMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        JsParamMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.array = [];
            if (options.defaults) {
                object.primitive = "";
                object.jsObject = null;
                object.javaObject = null;
            }
            if (message.primitive != null && message.hasOwnProperty("primitive"))
                object.primitive = message.primitive;
            if (message.jsObject != null && message.hasOwnProperty("jsObject"))
                object.jsObject = $root.appFrameProtoIn.JSObjectMsgInProto.toObject(message.jsObject, options);
            if (message.javaObject != null && message.hasOwnProperty("javaObject"))
                object.javaObject = $root.appFrameProtoIn.JavaObjectRefMsgInProto.toObject(message.javaObject, options);
            if (message.array && message.array.length) {
                object.array = [];
                for (var j = 0; j < message.array.length; ++j)
                    object.array[j] = $root.appFrameProtoIn.JsParamMsgInProto.toObject(message.array[j], options);
            }
            return object;
        };

        /**
         * Converts this JsParamMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.JsParamMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        JsParamMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return JsParamMsgInProto;
    })();

    appFrameProtoIn.JavaObjectRefMsgInProto = (function() {

        /**
         * Properties of a JavaObjectRefMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IJavaObjectRefMsgInProto
         * @property {string|null} [id] JavaObjectRefMsgInProto id
         * @property {Array.<string>|null} [methods] JavaObjectRefMsgInProto methods
         */

        /**
         * Constructs a new JavaObjectRefMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a JavaObjectRefMsgInProto.
         * @implements IJavaObjectRefMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IJavaObjectRefMsgInProto=} [properties] Properties to set
         */
        function JavaObjectRefMsgInProto(properties) {
            this.methods = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * JavaObjectRefMsgInProto id.
         * @member {string} id
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @instance
         */
        JavaObjectRefMsgInProto.prototype.id = "";

        /**
         * JavaObjectRefMsgInProto methods.
         * @member {Array.<string>} methods
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @instance
         */
        JavaObjectRefMsgInProto.prototype.methods = $util.emptyArray;

        /**
         * Creates a new JavaObjectRefMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @static
         * @param {appFrameProtoIn.IJavaObjectRefMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.JavaObjectRefMsgInProto} JavaObjectRefMsgInProto instance
         */
        JavaObjectRefMsgInProto.create = function create(properties) {
            return new JavaObjectRefMsgInProto(properties);
        };

        /**
         * Encodes the specified JavaObjectRefMsgInProto message. Does not implicitly {@link appFrameProtoIn.JavaObjectRefMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @static
         * @param {appFrameProtoIn.IJavaObjectRefMsgInProto} message JavaObjectRefMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        JavaObjectRefMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && message.hasOwnProperty("id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.methods != null && message.methods.length)
                for (var i = 0; i < message.methods.length; ++i)
                    writer.uint32(/* id 2, wireType 2 =*/18).string(message.methods[i]);
            return writer;
        };

        /**
         * Creates a JavaObjectRefMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.JavaObjectRefMsgInProto} JavaObjectRefMsgInProto
         */
        JavaObjectRefMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.JavaObjectRefMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.JavaObjectRefMsgInProto();
            if (object.id != null)
                message.id = String(object.id);
            if (object.methods) {
                if (!Array.isArray(object.methods))
                    throw TypeError(".appFrameProtoIn.JavaObjectRefMsgInProto.methods: array expected");
                message.methods = [];
                for (var i = 0; i < object.methods.length; ++i)
                    message.methods[i] = String(object.methods[i]);
            }
            return message;
        };

        /**
         * Creates a plain object from a JavaObjectRefMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @static
         * @param {appFrameProtoIn.JavaObjectRefMsgInProto} message JavaObjectRefMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        JavaObjectRefMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.methods = [];
            if (options.defaults)
                object.id = "";
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.methods && message.methods.length) {
                object.methods = [];
                for (var j = 0; j < message.methods.length; ++j)
                    object.methods[j] = message.methods[j];
            }
            return object;
        };

        /**
         * Converts this JavaObjectRefMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.JavaObjectRefMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        JavaObjectRefMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return JavaObjectRefMsgInProto;
    })();

    appFrameProtoIn.JSObjectMsgInProto = (function() {

        /**
         * Properties of a JSObjectMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IJSObjectMsgInProto
         * @property {string|null} [id] JSObjectMsgInProto id
         */

        /**
         * Constructs a new JSObjectMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a JSObjectMsgInProto.
         * @implements IJSObjectMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IJSObjectMsgInProto=} [properties] Properties to set
         */
        function JSObjectMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * JSObjectMsgInProto id.
         * @member {string} id
         * @memberof appFrameProtoIn.JSObjectMsgInProto
         * @instance
         */
        JSObjectMsgInProto.prototype.id = "";

        /**
         * Creates a new JSObjectMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.JSObjectMsgInProto
         * @static
         * @param {appFrameProtoIn.IJSObjectMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.JSObjectMsgInProto} JSObjectMsgInProto instance
         */
        JSObjectMsgInProto.create = function create(properties) {
            return new JSObjectMsgInProto(properties);
        };

        /**
         * Encodes the specified JSObjectMsgInProto message. Does not implicitly {@link appFrameProtoIn.JSObjectMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.JSObjectMsgInProto
         * @static
         * @param {appFrameProtoIn.IJSObjectMsgInProto} message JSObjectMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        JSObjectMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && message.hasOwnProperty("id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            return writer;
        };

        /**
         * Creates a JSObjectMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.JSObjectMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.JSObjectMsgInProto} JSObjectMsgInProto
         */
        JSObjectMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.JSObjectMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.JSObjectMsgInProto();
            if (object.id != null)
                message.id = String(object.id);
            return message;
        };

        /**
         * Creates a plain object from a JSObjectMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.JSObjectMsgInProto
         * @static
         * @param {appFrameProtoIn.JSObjectMsgInProto} message JSObjectMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        JSObjectMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.id = "";
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            return object;
        };

        /**
         * Converts this JSObjectMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.JSObjectMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        JSObjectMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return JSObjectMsgInProto;
    })();

    appFrameProtoIn.PixelsAreaResponseMsgInProto = (function() {

        /**
         * Properties of a PixelsAreaResponseMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IPixelsAreaResponseMsgInProto
         * @property {string|null} [correlationId] PixelsAreaResponseMsgInProto correlationId
         * @property {string|null} [pixels] PixelsAreaResponseMsgInProto pixels
         */

        /**
         * Constructs a new PixelsAreaResponseMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a PixelsAreaResponseMsgInProto.
         * @implements IPixelsAreaResponseMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IPixelsAreaResponseMsgInProto=} [properties] Properties to set
         */
        function PixelsAreaResponseMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PixelsAreaResponseMsgInProto correlationId.
         * @member {string} correlationId
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @instance
         */
        PixelsAreaResponseMsgInProto.prototype.correlationId = "";

        /**
         * PixelsAreaResponseMsgInProto pixels.
         * @member {string} pixels
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @instance
         */
        PixelsAreaResponseMsgInProto.prototype.pixels = "";

        /**
         * Creates a new PixelsAreaResponseMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @static
         * @param {appFrameProtoIn.IPixelsAreaResponseMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.PixelsAreaResponseMsgInProto} PixelsAreaResponseMsgInProto instance
         */
        PixelsAreaResponseMsgInProto.create = function create(properties) {
            return new PixelsAreaResponseMsgInProto(properties);
        };

        /**
         * Encodes the specified PixelsAreaResponseMsgInProto message. Does not implicitly {@link appFrameProtoIn.PixelsAreaResponseMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @static
         * @param {appFrameProtoIn.IPixelsAreaResponseMsgInProto} message PixelsAreaResponseMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PixelsAreaResponseMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.correlationId != null && message.hasOwnProperty("correlationId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.correlationId);
            if (message.pixels != null && message.hasOwnProperty("pixels"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.pixels);
            return writer;
        };

        /**
         * Creates a PixelsAreaResponseMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.PixelsAreaResponseMsgInProto} PixelsAreaResponseMsgInProto
         */
        PixelsAreaResponseMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.PixelsAreaResponseMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.PixelsAreaResponseMsgInProto();
            if (object.correlationId != null)
                message.correlationId = String(object.correlationId);
            if (object.pixels != null)
                message.pixels = String(object.pixels);
            return message;
        };

        /**
         * Creates a plain object from a PixelsAreaResponseMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @static
         * @param {appFrameProtoIn.PixelsAreaResponseMsgInProto} message PixelsAreaResponseMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PixelsAreaResponseMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.correlationId = "";
                object.pixels = "";
            }
            if (message.correlationId != null && message.hasOwnProperty("correlationId"))
                object.correlationId = message.correlationId;
            if (message.pixels != null && message.hasOwnProperty("pixels"))
                object.pixels = message.pixels;
            return object;
        };

        /**
         * Converts this PixelsAreaResponseMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.PixelsAreaResponseMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PixelsAreaResponseMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return PixelsAreaResponseMsgInProto;
    })();

    appFrameProtoIn.WindowEventMsgInProto = (function() {

        /**
         * Properties of a WindowEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IWindowEventMsgInProto
         * @property {string|null} [id] WindowEventMsgInProto id
         * @property {number|null} [x] WindowEventMsgInProto x
         * @property {number|null} [y] WindowEventMsgInProto y
         * @property {number|null} [width] WindowEventMsgInProto width
         * @property {number|null} [height] WindowEventMsgInProto height
         * @property {appFrameProtoIn.WindowEventMsgInProto.WindowEventTypeProto|null} [eventType] WindowEventMsgInProto eventType
         */

        /**
         * Constructs a new WindowEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents a WindowEventMsgInProto.
         * @implements IWindowEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IWindowEventMsgInProto=} [properties] Properties to set
         */
        function WindowEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * WindowEventMsgInProto id.
         * @member {string} id
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         */
        WindowEventMsgInProto.prototype.id = "";

        /**
         * WindowEventMsgInProto x.
         * @member {number} x
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         */
        WindowEventMsgInProto.prototype.x = 0;

        /**
         * WindowEventMsgInProto y.
         * @member {number} y
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         */
        WindowEventMsgInProto.prototype.y = 0;

        /**
         * WindowEventMsgInProto width.
         * @member {number} width
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         */
        WindowEventMsgInProto.prototype.width = 0;

        /**
         * WindowEventMsgInProto height.
         * @member {number} height
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         */
        WindowEventMsgInProto.prototype.height = 0;

        /**
         * WindowEventMsgInProto eventType.
         * @member {appFrameProtoIn.WindowEventMsgInProto.WindowEventTypeProto} eventType
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         */
        WindowEventMsgInProto.prototype.eventType = 0;

        /**
         * Creates a new WindowEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IWindowEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.WindowEventMsgInProto} WindowEventMsgInProto instance
         */
        WindowEventMsgInProto.create = function create(properties) {
            return new WindowEventMsgInProto(properties);
        };

        /**
         * Encodes the specified WindowEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.WindowEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IWindowEventMsgInProto} message WindowEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        WindowEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && message.hasOwnProperty("id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.x != null && message.hasOwnProperty("x"))
                writer.uint32(/* id 2, wireType 0 =*/16).sint32(message.x);
            if (message.y != null && message.hasOwnProperty("y"))
                writer.uint32(/* id 3, wireType 0 =*/24).sint32(message.y);
            if (message.width != null && message.hasOwnProperty("width"))
                writer.uint32(/* id 4, wireType 0 =*/32).sint32(message.width);
            if (message.height != null && message.hasOwnProperty("height"))
                writer.uint32(/* id 5, wireType 0 =*/40).sint32(message.height);
            if (message.eventType != null && message.hasOwnProperty("eventType"))
                writer.uint32(/* id 6, wireType 0 =*/48).int32(message.eventType);
            return writer;
        };

        /**
         * Creates a WindowEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.WindowEventMsgInProto} WindowEventMsgInProto
         */
        WindowEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.WindowEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.WindowEventMsgInProto();
            if (object.id != null)
                message.id = String(object.id);
            if (object.x != null)
                message.x = object.x | 0;
            if (object.y != null)
                message.y = object.y | 0;
            if (object.width != null)
                message.width = object.width | 0;
            if (object.height != null)
                message.height = object.height | 0;
            switch (object.eventType) {
            case "close":
            case 0:
                message.eventType = 0;
                break;
            case "focus":
            case 1:
                message.eventType = 1;
                break;
            case "maximize":
            case 2:
                message.eventType = 2;
                break;
            case "undecorate":
            case 3:
                message.eventType = 3;
                break;
            case "decorate":
            case 4:
                message.eventType = 4;
                break;
            case "undock":
            case 5:
                message.eventType = 5;
                break;
            case "dock":
            case 6:
                message.eventType = 6;
                break;
            }
            return message;
        };

        /**
         * Creates a plain object from a WindowEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @static
         * @param {appFrameProtoIn.WindowEventMsgInProto} message WindowEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        WindowEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.x = 0;
                object.y = 0;
                object.width = 0;
                object.height = 0;
                object.eventType = options.enums === String ? "close" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.x != null && message.hasOwnProperty("x"))
                object.x = message.x;
            if (message.y != null && message.hasOwnProperty("y"))
                object.y = message.y;
            if (message.width != null && message.hasOwnProperty("width"))
                object.width = message.width;
            if (message.height != null && message.hasOwnProperty("height"))
                object.height = message.height;
            if (message.eventType != null && message.hasOwnProperty("eventType"))
                object.eventType = options.enums === String ? $root.appFrameProtoIn.WindowEventMsgInProto.WindowEventTypeProto[message.eventType] : message.eventType;
            return object;
        };

        /**
         * Converts this WindowEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.WindowEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        WindowEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * WindowEventTypeProto enum.
         * @name appFrameProtoIn.WindowEventMsgInProto.WindowEventTypeProto
         * @enum {string}
         * @property {number} close=0 close value
         * @property {number} focus=1 focus value
         * @property {number} maximize=2 maximize value
         * @property {number} undecorate=3 undecorate value
         * @property {number} decorate=4 decorate value
         * @property {number} undock=5 undock value
         * @property {number} dock=6 dock value
         */
        WindowEventMsgInProto.WindowEventTypeProto = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "close"] = 0;
            values[valuesById[1] = "focus"] = 1;
            values[valuesById[2] = "maximize"] = 2;
            values[valuesById[3] = "undecorate"] = 3;
            values[valuesById[4] = "decorate"] = 4;
            values[valuesById[5] = "undock"] = 5;
            values[valuesById[6] = "dock"] = 6;
            return values;
        })();

        return WindowEventMsgInProto;
    })();

    appFrameProtoIn.AudioEventMsgInProto = (function() {

        /**
         * Properties of an AudioEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IAudioEventMsgInProto
         * @property {string|null} [id] AudioEventMsgInProto id
         * @property {boolean|null} [stop] AudioEventMsgInProto stop
         * @property {boolean|null} [ping] AudioEventMsgInProto ping
         */

        /**
         * Constructs a new AudioEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents an AudioEventMsgInProto.
         * @implements IAudioEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IAudioEventMsgInProto=} [properties] Properties to set
         */
        function AudioEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * AudioEventMsgInProto id.
         * @member {string} id
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @instance
         */
        AudioEventMsgInProto.prototype.id = "";

        /**
         * AudioEventMsgInProto stop.
         * @member {boolean} stop
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @instance
         */
        AudioEventMsgInProto.prototype.stop = false;

        /**
         * AudioEventMsgInProto ping.
         * @member {boolean} ping
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @instance
         */
        AudioEventMsgInProto.prototype.ping = false;

        /**
         * Creates a new AudioEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IAudioEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.AudioEventMsgInProto} AudioEventMsgInProto instance
         */
        AudioEventMsgInProto.create = function create(properties) {
            return new AudioEventMsgInProto(properties);
        };

        /**
         * Encodes the specified AudioEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.AudioEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IAudioEventMsgInProto} message AudioEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AudioEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && message.hasOwnProperty("id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.stop != null && message.hasOwnProperty("stop"))
                writer.uint32(/* id 2, wireType 0 =*/16).bool(message.stop);
            if (message.ping != null && message.hasOwnProperty("ping"))
                writer.uint32(/* id 3, wireType 0 =*/24).bool(message.ping);
            return writer;
        };

        /**
         * Creates an AudioEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.AudioEventMsgInProto} AudioEventMsgInProto
         */
        AudioEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.AudioEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.AudioEventMsgInProto();
            if (object.id != null)
                message.id = String(object.id);
            if (object.stop != null)
                message.stop = Boolean(object.stop);
            if (object.ping != null)
                message.ping = Boolean(object.ping);
            return message;
        };

        /**
         * Creates a plain object from an AudioEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @static
         * @param {appFrameProtoIn.AudioEventMsgInProto} message AudioEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        AudioEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.stop = false;
                object.ping = false;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.stop != null && message.hasOwnProperty("stop"))
                object.stop = message.stop;
            if (message.ping != null && message.hasOwnProperty("ping"))
                object.ping = message.ping;
            return object;
        };

        /**
         * Converts this AudioEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.AudioEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        AudioEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return AudioEventMsgInProto;
    })();

    appFrameProtoIn.ActionEventMsgInProto = (function() {

        /**
         * Properties of an ActionEventMsgInProto.
         * @memberof appFrameProtoIn
         * @interface IActionEventMsgInProto
         * @property {string|null} [actionName] ActionEventMsgInProto actionName
         * @property {string|null} [data] ActionEventMsgInProto data
         * @property {Uint8Array|null} [binaryData] ActionEventMsgInProto binaryData
         * @property {string|null} [windowId] ActionEventMsgInProto windowId
         * @property {appFrameProtoIn.ActionEventMsgInProto.ActionEventTypeProto|null} [eventType] ActionEventMsgInProto eventType
         */

        /**
         * Constructs a new ActionEventMsgInProto.
         * @memberof appFrameProtoIn
         * @classdesc Represents an ActionEventMsgInProto.
         * @implements IActionEventMsgInProto
         * @constructor
         * @param {appFrameProtoIn.IActionEventMsgInProto=} [properties] Properties to set
         */
        function ActionEventMsgInProto(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ActionEventMsgInProto actionName.
         * @member {string} actionName
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @instance
         */
        ActionEventMsgInProto.prototype.actionName = "";

        /**
         * ActionEventMsgInProto data.
         * @member {string} data
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @instance
         */
        ActionEventMsgInProto.prototype.data = "";

        /**
         * ActionEventMsgInProto binaryData.
         * @member {Uint8Array} binaryData
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @instance
         */
        ActionEventMsgInProto.prototype.binaryData = $util.newBuffer([]);

        /**
         * ActionEventMsgInProto windowId.
         * @member {string} windowId
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @instance
         */
        ActionEventMsgInProto.prototype.windowId = "";

        /**
         * ActionEventMsgInProto eventType.
         * @member {appFrameProtoIn.ActionEventMsgInProto.ActionEventTypeProto} eventType
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @instance
         */
        ActionEventMsgInProto.prototype.eventType = 0;

        /**
         * Creates a new ActionEventMsgInProto instance using the specified properties.
         * @function create
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IActionEventMsgInProto=} [properties] Properties to set
         * @returns {appFrameProtoIn.ActionEventMsgInProto} ActionEventMsgInProto instance
         */
        ActionEventMsgInProto.create = function create(properties) {
            return new ActionEventMsgInProto(properties);
        };

        /**
         * Encodes the specified ActionEventMsgInProto message. Does not implicitly {@link appFrameProtoIn.ActionEventMsgInProto.verify|verify} messages.
         * @function encode
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @static
         * @param {appFrameProtoIn.IActionEventMsgInProto} message ActionEventMsgInProto message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ActionEventMsgInProto.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.actionName != null && message.hasOwnProperty("actionName"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.actionName);
            if (message.data != null && message.hasOwnProperty("data"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.data);
            if (message.binaryData != null && message.hasOwnProperty("binaryData"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.binaryData);
            if (message.windowId != null && message.hasOwnProperty("windowId"))
                writer.uint32(/* id 4, wireType 2 =*/34).string(message.windowId);
            if (message.eventType != null && message.hasOwnProperty("eventType"))
                writer.uint32(/* id 5, wireType 0 =*/40).int32(message.eventType);
            return writer;
        };

        /**
         * Creates an ActionEventMsgInProto message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {appFrameProtoIn.ActionEventMsgInProto} ActionEventMsgInProto
         */
        ActionEventMsgInProto.fromObject = function fromObject(object) {
            if (object instanceof $root.appFrameProtoIn.ActionEventMsgInProto)
                return object;
            var message = new $root.appFrameProtoIn.ActionEventMsgInProto();
            if (object.actionName != null)
                message.actionName = String(object.actionName);
            if (object.data != null)
                message.data = String(object.data);
            if (object.binaryData != null)
                if (typeof object.binaryData === "string")
                    $util.base64.decode(object.binaryData, message.binaryData = $util.newBuffer($util.base64.length(object.binaryData)), 0);
                else if (object.binaryData.length)
                    message.binaryData = object.binaryData;
            if (object.windowId != null)
                message.windowId = String(object.windowId);
            switch (object.eventType) {
            case "init":
            case 0:
                message.eventType = 0;
                break;
            case "user":
            case 1:
                message.eventType = 1;
                break;
            }
            return message;
        };

        /**
         * Creates a plain object from an ActionEventMsgInProto message. Also converts values to other types if specified.
         * @function toObject
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @static
         * @param {appFrameProtoIn.ActionEventMsgInProto} message ActionEventMsgInProto
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ActionEventMsgInProto.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.actionName = "";
                object.data = "";
                if (options.bytes === String)
                    object.binaryData = "";
                else {
                    object.binaryData = [];
                    if (options.bytes !== Array)
                        object.binaryData = $util.newBuffer(object.binaryData);
                }
                object.windowId = "";
                object.eventType = options.enums === String ? "init" : 0;
            }
            if (message.actionName != null && message.hasOwnProperty("actionName"))
                object.actionName = message.actionName;
            if (message.data != null && message.hasOwnProperty("data"))
                object.data = message.data;
            if (message.binaryData != null && message.hasOwnProperty("binaryData"))
                object.binaryData = options.bytes === String ? $util.base64.encode(message.binaryData, 0, message.binaryData.length) : options.bytes === Array ? Array.prototype.slice.call(message.binaryData) : message.binaryData;
            if (message.windowId != null && message.hasOwnProperty("windowId"))
                object.windowId = message.windowId;
            if (message.eventType != null && message.hasOwnProperty("eventType"))
                object.eventType = options.enums === String ? $root.appFrameProtoIn.ActionEventMsgInProto.ActionEventTypeProto[message.eventType] : message.eventType;
            return object;
        };

        /**
         * Converts this ActionEventMsgInProto to JSON.
         * @function toJSON
         * @memberof appFrameProtoIn.ActionEventMsgInProto
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ActionEventMsgInProto.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * ActionEventTypeProto enum.
         * @name appFrameProtoIn.ActionEventMsgInProto.ActionEventTypeProto
         * @enum {string}
         * @property {number} init=0 init value
         * @property {number} user=1 user value
         */
        ActionEventMsgInProto.ActionEventTypeProto = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "init"] = 0;
            values[valuesById[1] = "user"] = 1;
            return values;
        })();

        return ActionEventMsgInProto;
    })();

    return appFrameProtoIn;
})();

module.exports = $root;
