/* 
 * Copyright Lumens Team, Inc. All Rights Reserved.
 */

Lumens.TreeNode = Class.$extend({
    __init__: function(type, label, name, data, parent, classes) {
        var __this = this;
        this.children = {size: 0, map: {}};
        this.$parent = parent;
        this.nodeType = type;
        this.label = label;
        this.name = name;
        this.data = data;
        this.clickHandler = parent.clickHandler;
        this.dblclickHandler = parent.dblclickHandler;
        this.dropHandler = parent.dropHandler;
        this.$container = parent.getElement();
        this.levelNumber = parent.levelNumber + 1;
        this.indexNumber = parent.children.size;
        this.draggable = parent.draggable;
        this.droppable = parent.droppable;
        this.$folder = $('<div class="lumens-tree-folder" style="display: block;"></div>').appendTo(this.$container).attr('id', '(' + this.levelNumber + ',' + this.indexNumber + ')');
        if (classes)
            this.$folder.addClass(classes);
        // ----------- Build folder header ----------------
        if (this.nodeType === "file") {
            this.$fHeader = $('<div class="lumens-tree-folder-header"><div class="lumens-tree-node" ><i class="lumens-icon-file-node"></i><div class="lumens-tree-node-name"></div><div class="lumens-tree-node-script"></div></div></div>').appendTo(this.$folder);
        }
        else {
            this.$fHeader = $('<div class="lumens-tree-folder-header"><i id="folder-status" class="lumens-icon-folder-close"/><div class="lumens-tree-node"><i class="lumens-icon-folder-node"/><div class="lumens-tree-node-name"></div><div class="lumens-tree-node-script"></div></div></div>').appendTo(this.$folder);
            this.$fContent = $('<div class="lumens-tree-folder-content" style="display:none;"></div>').appendTo(this.$folder);
        }
        //-------------------------------------------------
        this.$fHeader.find('.lumens-tree-node-name').html(label);
        if (this.data.script)
            this.$fHeader.find('.lumens-tree-node-script').html(this.data.script);
        if (this.draggable)
            this.$fHeader.find(".lumens-tree-node").draggable({
                appendTo: "body",
                helper: "clone"
            }).data("tree-node-data", {child: data, location: this.getLocationPath(this)});
        if (this.droppable) {
            this.$fHeader.find(".lumens-tree-node").droppable({
                greedy: true,
                hoverClass: "lumens-tree-node-hover",
                accept: ".lumens-tree-node",
                drop: function(event, ui) {
                    var data = $.data(ui.draggable.get(0), "tree-node-data");
                    if (__this.dropHandler)
                        __this.dropHandler(data, __this, __this.$parent);
                }
            });
        }
        this.$fHeader.click(function(evt) {
            evt.stopPropagation();
            if (__this.clickHandler)
                __this.clickHandler(__this, __this.$parent);
        });
        this.$fHeader.dblclick(function(evt) {
            evt.stopPropagation();
            if (__this.dblclickHandler)
                __this.dblclickHandler(__this, __this.$parent);
            __this.toggleContent();
        });
    },
    getLocationPath: function(node) {
        var path = [];
        // TODO need to handle new tree root node
        node = node.$parent;
        while (node && node.data) {
            path.push({
                form: node.data.form,
                name: node.data.name,
                type: node.data.type
            });
            node = node.$parent;
        }
        path.reverse();
        if (path.length === 0)
            path.push({
                form: this.data.form,
                name: this.data.name,
                type: this.data.type
            });
        return path;
    },
    getRoot: function() {
        var node = this;
        while (node.$parent && node.$parent.data)
            node = node.$parent;
        return node;
    },
    getScript: function() {
        return this.data.script;
    },
    setScript: function(script) {
        this.data.script = script;
        this.$fHeader.find('.lumens-tree-node-script').html(this.data.script);
    },
    getElement: function() {
        return this.$fContent ? this.$fContent : this.$folder;
    },
    isFolder: function() {
        return this.nodeType !== "file";
    },
    hasContent: function() {
        return  this.isFolder() && this.$fContent.children().length > 0;
    },
    removeContent: function() {
        this.$fContent.empty();
    },
    getLabel: function() {
        return this.$fHeader.find('.lumens-tree-node-name').text();
    },
    getName: function() {
        return this.name;
    },
    getId: function() {
        return '(' + this.levelNumber + ',' + this.indexNumber + ')';
    },
    getLevel: function() {
        return this.levelNumber;
    },
    toggleContent: function() {
        if (this.isFolder() && this.hasContent() > 0) {
            var $status = this.$fHeader.find('#folder-status');
            if ($status.hasClass('lumens-icon-folder-open')) {
                $status.removeClass('lumens-icon-folder-open').addClass('lumens-icon-folder-close');
                this.$fContent.hide("blind", 200);
            }
            else {
                this.$fContent.show("blind", 200);
                $status.removeClass('lumens-icon-folder-close').addClass('lumens-icon-folder-open');
            }
        }
    },
    addChildList: function(nodes) {
        if (!this.isFolder())
            throw "Current node is not folder type, no child";
        var parent = this;
        $.each(nodes, function() {
            var entry = new Lumens.TreeNode(this.nodeType, this.label, this.name, this.data, parent);
            parent.children.map[entry.getName()] = entry;
            parent.children.size++;
        });
        return this;
    }
});

Lumens.Tree = Class.$extend({
    __init__: function(container) {
        this.$parentContainer = container;
        this.$tree = $('<div class="lumens-tree"/>').appendTo(container);
        this.children = {size: 0, map: {}};
        this.levelNumber = -1;
    },
    getElement: function() {
        return this.$tree;
    },
    addEntryList: function(entryList) {
        var parent = this;
        $.each(entryList, function() {
            var entry = new Lumens.TreeNode(this.nodeType, this.label, this.name, this.data, parent, parent.children.size ? "lumens-0n-level" : "lumens-00-level");
            parent.children.map[entry.getName()] = entry;
            parent.children.size++;
        });
        return this;
    },
    configure: function(config) {
        if (config) {
            this.draggable = config.draggable;
            this.droppable = config.droppable;
            if (config.classes)
                this.$tree.addClass(classes);
            if (config.click)
                this.clickHandler = config.click;
            if (config.dblclick)
                this.dblclickHandler = config.dblclick;
            if (config.drop)
                this.dropHandler = config.drop;
            if (config.handler)
                config.handler(this);
        }
        return this;
    }
});

Lumens.FormatTree = Class.$extend({
});

Lumens.TransformRuleTree = Class.$extend({
});
