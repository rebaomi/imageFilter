/**
 * Created by Administrator on 2015/2/28.
 */
/**
 * The MIT License (MIT)

 Copyright (c) 2013 腾讯 AlloyTeam
 http://www.alloyteam.com
 http://alloyteam.github.io/

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * @description: Main
 * @version 0.0.1
 * @author AlloyTeam & lvxd （原作者是腾讯的前端团队AlloyTeam@link:http://alloyteam.github.io/AlloyPhoto/，修改者为lvxd）
 *
 */

//删除几个元素   arr为数组下标
//Delete some elements from Array, param arr stands for the indexes of elements being deleted
Array.prototype.del = function(arr){

    //对数组重新排序
    //Sort array
    arr.sort();

    //复制数组，防止污染
    //Clone array in case of being modified
    var b = this.concat([]);
    for(var i = arr.length - 1; i >= 0; i --){
        b = b.slice(0, arr[i]).concat(b.slice(arr[i] + 1));
    }

    return b;
};

//worker适配
//For web worker adapting different cases
try{

    //给图像对象添加初次加载才触发事件，后续不触发
    HTMLImageElement.prototype.loadOnce = function(func){
        var i = 0;
        this.onload = function(){
            if(!i) func.call(this, null);
            i ++;
        };
    };
}catch(e){
    window = {};
}

var window = exports.window = window || {};
var Ps = exports.Ps = "Pslib";
var Canvas = exports.Canvas = require('canvas');
var Image = exports.Image = Canvas.Image;

var device = function(){
    if(window.navigator){
        var ua = window.navigator.userAgent;

        if(/Android|android/.test(ua)){
            return 'android';
        }else if(/iPhone|iPad|iPod/.test(ua)){
            return 'ios';
        }else{
            return 'other';
        }
    }else{
        return "sandBox";
    }
}();

//被所有对象引用的一个对象,静态对象,主处理模块
//主处理模块要求与DOM操作无关
var P = {

    //模块池
    lib: [],

    //外部定义的ps效果
    definedPs: {},

    //初始化准备
    init: function(){
        this.require("config");
    },

    //模块注册方法
    module: function(name, func){
        var moduleArr = [name];
        if(/\./g.test(name)){
            moduleArr = name.split(".");
        }

        var count = -1, _this = this;
        function addModule(obj){
            count ++;

            var attr = moduleArr[count];

            //递归出口
            if(count == moduleArr.length - 1){
                obj[attr] = func.call(null, _this);

                return;
            }

            obj[attr] ? addModule(obj[attr]) : addModule(obj[attr] = {});
        }

        addModule(this.lib);

    },

    //加载文件
    require: function(name){
//            var _this = this;
//            var scriptLoader = document.createElement("script");
//
//            document.body.appendChild(scriptLoader);
//            scriptLoader.src = "./js/module/" + name + ".js";
//            scriptLoader.onload = scriptLoader.onerror = function(e){
//                _this.handlerror(e);
//            }
        require("./module/" + name + "js");
    },

    //错误处理部分
    handlerror: function(e){
        this.destroySelf("程序因未知原因中断");
    },

    //程序被迫自杀，杀前请留下遗嘱
    destroySelf: function(msg){
        delete window[Ps];
        var e = new Error(msg);
        throw(e);
    },

    //映射器,将中文方法或...映射为实际方法
    reflect: function(method, imgData, args){

        //得到实际的模块名称
        var moduleName = this.lib.config.getModuleName(method);

        var spaceName = moduleName.spaceName;
        var actName = moduleName.actName;

        switch(spaceName){
            case "Filter":
            case "Alteration":

                return this.lib[spaceName][actName].process(imgData, args);
            //break;

            case "ComEffect":
                return this.lib[actName].process(imgData, args);
            //break;

            default:
                //逻辑几乎不会到这里 出于好的习惯，加上default
                this.destroySelf("AI_ERROR: ");
        }
    },

    //组合效果映射器
    reflectEasy: function(effect){
        var fun = this.lib.config.getEasyFun(effect).actName;
        return this.definedPs[effect] || this.lib.easy.getFun(fun);
    },

    //合并一个图层到对象
    add: function(lowerData, upperData, method, alpha, dx, dy, isFast, channel){
        return this.lib.addLayer.add(lowerData, upperData, method, alpha, dx, dy, isFast, channel);
    },

    //用worker进行异步处理
    worker: function(func, callback){

    },

    //对图像进行掩模算子变换
    applyMatrix: function(imgData, matrixArr){
    },

    //args[0]代表处理方法，args[1...]代表参数
    tools: function(imgData, args){
        var actMethod = Array.prototype.shift.call(args);

        if(this.lib.Tools[actMethod]){
            return this.lib.Tools[actMethod].process(imgData, args);
        }else{
            throw new Error("AI_ERROR: 不存在的工具方法_" + actMethod);
        }
    },

    definePs: function(name, func){
        this.definedPs[name] = func;
    }
};

exports.P = P;
var Canvas = require('canvas'),
    Image = Canvas.Image;

//返回外部接口
window[Ps] = function(img, width, height){
    var _this = this;

    if(this instanceof window[Ps]){
        //记录时间 time trace
        this.startTime = + new Date();
        var canvas = new Canvas();
//            var canvas = document.createElement("canvas");
        var context = canvas.getContext("2d");

        //var l = psLib(20,30);构造适配
        if(!isNaN(img)){

            canvas.width = img;
            canvas.height = width;
//                canvas.width = width;
//                canvas.height = height;
            height = height || "#fff";
            context.fillStyle = height;
            context.fillRect(0, 0, img, width);
//                context.fillRect(0, 0, width, height);

            this.srcImg = "";

        }else if(typeof img == "string"){
            var tmpImg = new Image;
            tmpImg.onload = function(){
                canvas.width = parseInt(this.width);
                canvas.height = parseInt(this.height);

                context.drawImage(this, 0, 0, this.width, this.height);
            };
            tmpImg.src = img;
        }else{
            var dw = width, dh = height;

            var sw = img.width, sh = img.height;
            var ratio = sw / sh;

            if(width || height){
                if(! height){
                    dh = ~~ (dw / ratio);
                }else if(! width){
                    dw = dh * ratio;
                }
            }else{
                dw = sw;
                dh = sh;
            }

            canvas.width = dw;
            canvas.height = dh;

            if(! isNaN(dw)){
                if(device == "ios"){
                    P.lib.Fix.drawImageIOS(context, img, dw, dh);
                }else{
//                        img.onLoad = function(){
//                            context.drawImage(img, 0, 0, dw, dh);
//                        }
//                        img.onLoad();
                    context.drawImage(img, 0, 0, dw, dh);
                }

            }else context.drawImage(img, 0, 0);

            this.srcImg = img;

        }

        //将引用的canvas对象挂接到对象上
        this.canvas = canvas;
        this.context = context;
        this.imgData = context.getImageData(0, 0, canvas.width, canvas.height);

        //赋予对象唯一ID
        this.name = Ps + "_" + Math.random();
        this.canvas.id = this.name;

        //记录挂接到图层上的对象的引用
        this.layers = [];

        //原生canvas支持时的临时canvas
        var ctxCanvas = new Canvas();
        ctxCanvas.width = canvas.width;
        ctxCanvas.height = canvas.height;

        this.ctxCanvas = ctxCanvas;
        this.ctxContext = canvas.getContext("2d");

        //设置对象的宽高
        this.width = this.canvas.width;
        this.height = this.canvas.height;

        //默认使用worker进行处理
        this.useWorker = P.useWorker;

        //初始化readyState为ready,readyState表明处理就绪
        this.readyState = 1;

        if(this.useWorker){
            //如果使用worker,则初始化一个dorsyWorker封装实例出来
            this.dorsyWorker = P.lib.dorsyWorker(this);
        }

        //mix P.lib.Tools method to $AI.Tools
        if(P.lib.Tools){
            for(var i in P.lib.Tools){
                this.Tools[i] = (function(i){
                    return function(args){
                        return _this.Tools(i, args);
                    };
                })(i);
            }
        }

    }else{

        //返回自身构造对象
        return new window[Ps](img, width, height);
    }
};

//模块注册方法
window[Ps].module = function(name, func){
    P.module(name, func);
};

//返回一个外部独立的数学处理模式出去
window[Ps].dorsyMath = function(){
    return P.lib.dorsyMath;
};

window[Ps].pixelUtils = function(){
    return P.lib.pixelUtils;
};

window[Ps].setName = function(name){
    P.name = name || "alloyimage.js";
};

//获取配置信息
window[Ps].getConfig = function(){
    return P.lib.config.getConfig();
};

//定义组合效果代码
window[Ps].define = function(name, func){
    P.definePs(name, func);
};

//定义使用worker,需要给出alloyimage所在路径
window[Ps].useWorker = function(path){

    //如果不能使用worker，直接降级为单线程
    if(! window.Worker){
        this.useWorker = 0;

        console.log("AI_WARNING: 浏览器不支持web worker, 自动切换为单线程\nAI_WARNING: the brower doesn't support Web Worker");
        return;
    }

    var path = path || "";

    //如果以目录给出，默认为默认文件名
    if(/[\/\\]$/.test(path)){
        path = path + P.name;
    }else{
    }

    if(path == "") path = "alloyimage.js";

    P.useWorker = 1;
    P.path = path;

    //检测文件是否存在
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){

        if(xhr.readyState == 4){

            if(xhr.status == "404"){
                P.destroySelf("AI_ERROR：使用worker时，ai文件路径指定错误\nAI_ERROR: error occured while using web worker since indicate the wrong path of file ai");
            }
        }
    };
    xhr.open("GET", path, false);
    xhr.send();
};

//worker监听
onmessage = function(e){
    var data = e.data, imgData;
    if(data[0] == "act"){
        imgData = P.reflect(data[1], data[2], data[3]);
    }else if(data[0] == "add"){
        imgData = P.add.apply(P, data[1]);
    }
    postMessage(imgData);
};

//原型对象
window[Ps].prototype = {
    set width(w){
        this.canvas.width = w;
    },

    set height(h){
        this.canvas.height = h;
    },

    get width(){
        return this.canvas.width;
    },

    get height(){
        return this.canvas.height;
    },

    //动作
    act: function(method, arg){
        //console.log("actStart");
        var args = [];

        //提取参数为数组
        args = Array.prototype.slice.call(arguments, 1);

        if(this.useWorker){
            this.dorsyWorker.queue.push(["act", method, args]);

            checkStartWorker.call(this);

        }else{
            //做一次转发映射
            P.reflect(method, this.imgData, args);

        }
        return this;
    },

    //预览模式 ，所有的再操作全部基于原点，不会改变本图层的效果，直到act会去除这部分图层
    view: function(method, arg1, arg2, arg3, arg4){

        //克隆本图层对象
        var newLayer = this.clone();

        //标记本图层的种类为预览的已合并的图层
        newLayer.type = 1;

        //挂接克隆图层副本到对象
        this.addLayer(newLayer, "正常", 0, 0);
        newLayer.act(method, arg1, arg2, arg3, arg4);

        return this;
    },

    //将view的结果执行到图层
    excute: function(){
        var layers = this.layers;
        var n = layers.length;
        if(layers[n - 1] && layers[n - 1][0].type == 1){
            this.imgData = layers[n - 1][0].imgData;
            delete layers[n - 1];
        }
    },

    //取消view的结果执行
    cancel: function(){
        var layers = this.layers;
        var n = layers.length;
        if(layers[n - 1] && layers[n - 1][0].type == 1){
            delete layers[n - 1];
        }
    },

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //显示对象 isFast用于快速显示
//        show: function(selector, isFast, flag){
//
//            if(flag){
//            }else{
//                if(this.useWorker){
//                    this.dorsyWorker.queue.push(["show", selector, isFast]);
//                    return this;
//                }
//            }
//
//            //如果其上无其他挂载图层，加快处理
//            if(this.layers.length == 0){
//                this.tempPsLib = {
//                    imgData: this.imgData
//                };
//            }else{
//
//                //创建一个临时的psLib对象，防止因为合并显示对本身imgData影响
//                var tempPsLib = new window[Ps](this.canvas.width, this.canvas.height);
//                tempPsLib.add(this, "正常", 0, 0, isFast);
//                this.tempPsLib = tempPsLib;
//
//                //将挂接到本对象上的图层对象 一起合并到临时的psLib对象上去 用于显示合并的结果，不会影响每个图层，包括本图层
//                for(var i = 0; i < this.layers.length; i ++){
//                    var tA = this.layers[i];
//                    var layers = tA[0].layers;
//                    var currLayer = tA[0];
//
//                    if(layers[layers.length - 1] && layers[layers.length - 1][0].type == 1) currLayer = layers[layers.length - 1][0];
//                    tempPsLib.add(currLayer, tA[1], tA[2], tA[3], isFast);
//                }
//
//                this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
//
//            }
//
//            //以临时对象data显示
//            this.context.putImageData(this.tempPsLib.imgData, 0, 0);
//
//            if(selector){
//                if(typeof selector == "string"){
//                    var el = document.querySelector(selector);
//                    el.appendChild(this.canvas);
//                }else{
//                    selector.appendChild(this.canvas);
//                }
//            }else{
//                document.body.appendChild(this.canvas);
//            }
//
//            return this;
//        },
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //替换到某元素里
//        replaceChild: function(selector){
//            var el;
//            if(typeof selector == "string"){
//                el = document.querySelector(selector);
//            }else{
//                el = selector;
//            }
//
//            el.innerHTML = "";
//            return this.show(el);
//        },
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //替换原来的图片
    replace: function(img, workerFlag){
        if(workerFlag){
        }else{
            if(this.useWorker){
                this.dorsyWorker.queue.push(['replace', img]);
                checkStartWorker.call(this);

                return this;
            }
        }

        if(img){
            img.onload = function(){};
            console.log();
            img.src = this.save(0, 1, workerFlag);
        }

        return this;
    },

    //合并一个AlloyImage图层上去
    add: function(){

        var numberArr = [], psLibObj, method, alpha, dx, dy, isFast, channel;

        //做重载
        for(var i = 0; i < arguments.length; i ++){
            if(!i) continue;

            switch(typeof(arguments[i])){
                case "string":
                    if(/\d+%/.test(arguments[i])){//alpha
                        alpha = arguments[i].replace("%", "");
                    }else if(/[RGB]+/.test(arguments[i])){//channel
                        channel = arguments[i];
                    }else{//method
                        method = arguments[i];
                    }
                    break;

                case "number":
                    numberArr.push(arguments[i]);
                    break;

                case "boolean":
                    isFast = arguments[i];
                    break;
            }
        }

        //赋值
        dx = numberArr[0] || 0;
        dy = numberArr[1] || 0;
        method = method || "正常";
        alpha = alpha / 100 || 1;
        isFast = isFast || false;
        channel = channel || "RGB";

        psLibObj = arguments[0];

        //console.log("add init");

        if(this.useWorker){
            this.dorsyWorker.queue.push(['add', psLibObj, method, alpha, dx, dy, isFast, channel]);

            checkStartWorker.call(this);

        }else{
            //做映射转发
            P.add(this.imgData, psLibObj.imgData, method, alpha, dx, dy, isFast, channel);
        }

        return this;
    },

    //挂载一个图层上去，不会影响本身，只是显示有变化
    addLayer: function(psLibObj, method, dx, dy){
        this.layers.push([psLibObj, method, dx, dy]);

        return this;
    },

    clone: function(workerFlag){

        /*
         if(workerFlag){
         }else{

         if(this.useWorker){
         this.dorsyWorker.queue.push(['clone']);
         return this;
         }
         }
         */

        var tempPsLib = new window[Ps](this.canvas.width, this.canvas.height);
        tempPsLib.context.putImageData(this.imgData, 0, 0);
        tempPsLib.imgData = tempPsLib.context.getImageData(0, 0, this.canvas.width, this.canvas.height);
        /*
         tempPsLib.add(this);
         */

        return tempPsLib;
    },

    //交换a,b图层的顺序,ab代表当前序号
    swap: function(a, b){
        var temp = this.layers[a];
        this.layers[a] = this.layers[b];
        this.layers[b] = temp;

        return this;
    },

    //删除几个图层序号
    deleteLayers: function(arr){
        this.layers = this.layers.del(arr);
    },

    //返回一个合成后的图像 png base64
    //comRatio为压缩质量
    save: function(type, comRatio, workerFlag){
        type = type || "png";
        type = type.toLowerCase();

        comRatio = comRatio || 0.8;

        if(type == "jpg") type = "jpeg";

        var mimeType = "image/" + type;

        if(workerFlag){
        }else{
            if(this.useWorker){
                this.dorsyWorker.queue.push(['save']);
                checkStartWorker.call(this);

                return this;
            }
        }

        //如果没有挂接图片 直接返回
        if(! this.layers.length){
            //将挂接到本对象上的图层对象 一起合并到临时的psLib对象上去 用于显示合并的结果，不会影响每个图层，包括本图层
            this.context.putImageData(this.imgData, 0, 0);
//                return this.canvas.toDataURL(mimeType, comRatio);
            var base64Str = this.canvas.toDataURL(mimeType, function(err,str){test = str;return str;});
            return this.canvas.toBuffer();
        }

        //创建一个临时的psLib对象，防止因为合并显示对本身imgData影响
        var tempPsLib = new window[Ps](this.canvas.width, this.canvas.height);
        tempPsLib.add(this, "正常", 0, 0, isFast);

        this.tempPsLib = tempPsLib;
        for(var i = 0; i < this.layers.length; i ++){
            var tA = this.layers[i];
            var layers = tA[0].layers;

            var currLayer = tA[0];
            if(layers[layers.length - 1] && layers[layers.length - 1][0].type == 1) currLayer = layers[layers.length - 1][0];

            tempPsLib.add(currLayer, tA[1], tA[2], tA[3], isFast);
        }


        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        //以临时对象data显示
        this.context.putImageData(tempPsLib.imgData, 0, 0);
//            return this.canvas.toDataURL(mimeType, comRatio);
        var base64Str = this.canvas.toDataURL(mimeType,function(err,str){return str;})
//            return this.canvas.toDataURL(mimeType,function(err,str){return str;});
        return this.canvas.toBuffer();
    },

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //下载图片
//        saveFile: function(fileName, comRatio){
//            fileName = fileName || "AlloyImage合成图像.jpg";
//            comRatio = comRatio || 1;
//
//            var formatReg = /.*.(jpg|png|gif|jpeg)$/g;
//            var format = "png";
//
//            if(formatReg.test(fileName)){
//                formatReg.lastIndex = 0;
//                format = formatReg.exec(fileName)[1];
//            }else{
//                fileName += ".png";
//            }
//
//            var fileData = this.save(format, comRatio);
//
//            var a = document.createElement('a');
//            a.href = fileData;
//            a.download = fileName;
//
//            var e = document.createEvent("HTMLEvents");
//            e.initEvent("click", false, false);
//
//            a.dispatchEvent(e);
//
//        },
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    download: function(fileName, comRatio){
        this.saveFile(fileName, comRatio);
    },

    saveAsDataURL: function(){
    },

    saveAsBlob: function(){
    },

    saveAsBuffer: function(){
    },
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //绘制直方图
//        drawRect: function(selector){
//            var canvas;
//
//            if(canvas = document.getElementById("imgRect")){
//            }else{
//                canvas = new Canvas();
//                canvas.id = "imgRect";
//                document.body.appendChild(canvas);
//                canvas.width = parseInt(this.canvas.width);
//                canvas.height = parseInt(this.canvas.height);
//            }
//
//            var context = canvas.getContext("2d");
//            context.clearRect(0, 0, canvas.width, canvas.height);
//
//            var result = [];
//            var data = this.tempPsLib.imgData.data;
//
//            for(var i = 0, n = data.length; i < n; i ++){
//                if(!result[data[i]]){
//                    result[data[i]] = 1;
//                }else{
//                    result[data[i]] ++;
//                }
//            }
//
//            context.beginPath();
//            context.moveTo(0, canvas.height);
//
//            var max = 0;
//
//            for(var i = 0; i < 255; i ++){
//                if(result[i] > max) max = result[i];
//            }
//
//            for(var i = 0; i < 255; i ++){
//                var currY = result[i] || 0;
//                currY = canvas.height - currY / max * 0.8 * canvas.height;
//                context.lineTo(i / 256 * canvas.width, currY, 1, 1);
//            }
//
//            context.lineTo(canvas.width + 10, canvas.height);
//            context.fill();
//        },
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //组合效果
    ps: function(effect){
        if(effect == "原图" || effect == "origin" || effect == ""){
            return this;
        }

        var fun = P.reflectEasy(effect);
        var psedPic = fun.call(this, this.canvas);

        //this.logTime("组合效果" + effect);

        return psedPic;
    },

    //记录运行时间
    logTime: function(msg){
        //console.log(msg + ": " + (+ new Date() - this.startTime) / 1000 + "s");
    },

    //调用原生canvas.context接口
    ctx: function(func){
        //func中的this指向context
        var ctx = this.ctxContext;

        ctx.putImageData(this.imgData, 0, 0);

        //调用func
        func.call(ctx);
        this.imgData = ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);

        return this;
    },

    notify: function(msg){
        //通知
        if(msg == "readyStateOK") this.readyState = 1;
    },

    //所有动作异步执行完了的回调
    complete: function(func){
        if(this.useWorker){
            //console.log("complete init");
            this.dorsyWorker.queue.push(['complete', func]);
        }else{
            func();
        }
    },

    //变换Matrix
    //x0, y0坐标原点
    transform: function(matrix, x0, y0){
        //获取dorsyMath接口
        var dM = window[Ps].dorsyMath();

        var ctx = this.ctxContext;
        ctx.putImageData(this.imgData, 0, 0);

        //建立一个空的临时canvas
        var tempCtx = new Canvas().getContext("2d");

        //计算变换后的canvas宽度
        //原则  所有的变换不会记录平移带来的变的换，但会记录下平移导致的原点的平移，以叠加图层的时候减去这些平移造成的影响
        //意味着图层自身所有的变换都不会丢失自己的图像信息 但会原点位置发生变化
        //这样做会节省很大的无图像空间

        //计算原有点变换后的点
        var originPoint = [
            new dM.Matrix([0, 0], "1*2"),
            new dM.Matrix([0, this.canvas.height], "1*2"),
            new dM.Matrix([this.canvas.width, 0], "1 * 2"),
            new dM.Matrix([this.canvas.width, this.canvas.height], "1*2")
        ];

        var transformedPoint = [];
        var transformMatrix = new dM.Matrix(matrix, "2*2");

        for(var i = 0; i < originPoint.length; i ++){
            transformedPoint.push(originPoint[i].mutiply(transformMatrix));
        }

        var maxX = Math.max(
            transformedPoint[0].data[0][0],
            transformedPoint[1].data[0][0],
            transformedPoint[2].data[0][0],
            transformedPoint[3].data[0][0]
        );

        var minX = Math.min(
            transformedPoint[0].data[0][0],
            transformedPoint[1].data[0][0],
            transformedPoint[2].data[0][0],
            transformedPoint[3].data[0][0]
        );

        var maxY = Math.max(
            transformedPoint[0].data[0][1],
            transformedPoint[1].data[0][1],
            transformedPoint[2].data[0][1],
            transformedPoint[3].data[0][1]
        );

        var minY = Math.min(
            transformedPoint[0].data[0][1],
            transformedPoint[1].data[0][1],
            transformedPoint[2].data[0][1],
            transformedPoint[3].data[0][1]
        );

        var width = ~~ (maxX - minX);
        var height = ~~ (maxY - minY);

        tempCtx.canvas.width = width;
        tempCtx.canvas.height = height;

        //将原点平移使图像显示出来 但图像的原点会发生变化
        tempCtx.translate(- minX, - minY);
        tempCtx.transform.apply(tempCtx, matrix);
        tempCtx.drawImage(ctx.canvas, 0, 0);

        this.canvas.width = width;
        this.canvas.height = height;

        this.width = width;
        this.height = height;

        this.imgData = tempCtx.getImageData(0, 0, width, height);

        return this;
    },

    scale: function(x, y){
        var y = y || x;
        return this.transform([x, 0, 0, y, 0, 0]);
    },

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        //缩放到宽度和高度
//        scaleTo: function(w, h){
//            var _this = this;
//            var width = this.width;
//            var height = this.height;
//
//            var scaleSizeX, scaleSizeY;
//
//            if(! h){
//                h = w * height / width;
//            }
//
//            if(! w){
//                w = h * (width / height);
//            }
//
//            //这里的代码在iphone上会导致倾斜
//            if(0 && this.srcImg){
//                var img = new Image();
//                img.width = w;
//                img.height = h;
//
//                document.body.appendChild(img);
//
//                img.style.width = w + "px";
//                img.style.height = ~~ h + "px";
//
//                img.src = this.srcImg.src;
//
//
//                var newAIObj = window[Ps](img, w, h);
//
//                document.body.removeChild(img);
//                img = null;
//
//                setTimeout(function(){
//                    if(_this.canvas.parentNode){
//                        _this.canvas.parentNode.replaceChild(newAIObj.canvas, _this.canvas);
//                        newAIObj.show();
//                    }
//                }, 10);
//
//                return newAIObj;
//
//            }else{
//
//                if(w && h){
//                    scaleSizeX = w / width;
//                    scaleSizeY = h / height;
//
//                    return this.scale(scaleSizeX, scaleSizeY);
//                }
//            }
//        },
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //旋转 度
    rotate: function(deg){
        //转为弧度
        var r = deg / 180 * Math.PI;
        var matrix = [
            Math.cos(r), Math.sin(r), -Math.sin(r), Math.cos(r), 0, 0
        ];

        return this.transform(matrix);
    },

    //平移
    moveTo: function(x, y){
        x = x || 0;
        y = y || 0;

        return this.transform([1, 0, 0, 1, x, y]);
    },

    //裁切
    clip: function(sx, sy, w, h){
        // @todo 多图层挂接支持

        //将图像信息放置于临时canvas上
        this.ctxContext.putImageData(this.imgData, 0, 0);

        //取到相关矩阵的图像信息
        this.imgData = this.ctxContext.getImageData(sx, sy, w, h);

        this.context.canvas.width = w;
        this.context.canvas.height = h;

        return this;
    },

    //图像的工具方法 不会返回AI本身
    Tools: function(args){
        return P.tools(this.imgData, arguments);
    }
};

//以下为AI所有的私有的方法,不需要公开 private methods

//检查是否要开始worker
function checkStartWorker(){

    //如果readyState为就绪状态 表明act为阶段首次动作,进入worker
    if(this.readyState){
        this.readyState = 0;
        this.dorsyWorker.startWorker();
    }
}

// exports
//exports.P.lib = P.lib;
//exports.psLib = exports.window[Ps]
//              = exports.$AI
//              = exports.AlloyImage
//              = window[Ps];
//
//exports.psLib.prototype = exports.window[Ps].prototype
//                        = exports.$AI.prototype
//                        = exports.AlloyImage.prototype
//                        = window[Ps].prototype;
exports.P.lib = P.lib;
exports.psLib = window[Ps];
exports.window[Ps] = window[Ps];
exports.psLib.prototype = window[Ps].prototype;
exports.window[Ps].prototype = window[Ps].prototype;
// sub module
require('./module/addLayer.js');
require('./module/applyMatrix.js');
require('./module/config.js');
require('./module/dorsyMath.js');
require('./module/dorsyWorker.js');
require('./module/easy.js');
require('./module/fix.js');
require('./module/tools.js');

// alteration
require('./module/alteration/brightness.js');
require('./module/alteration/curve.js');
require('./module/alteration/gamma.js');
require('./module/alteration/seletiveColor.js');
require('./module/alteration/setHSI.js');

// filter
require('./module/filter/corrode.js');
require('./module/filter/darkCorner.js');
require('./module/filter/dotted.js');
require('./module/filter/embossment.js');
require('./module/filter/gaussBlur.js');
require('./module/filter/ImageEnhance.js');
require('./module/filter/lapOfGauss.js');
require('./module/filter/mosaic.js');
require('./module/filter/noise.js');
require('./module/filter/oilPainting.js');
require('./module/filter/posterize.js');
require('./module/filter/sepia.js');
require('./module/filter/sharp.js');
require('./module/filter/toGray.js');
require('./module/filter/toReverse.js');
require('./module/filter/toThresh.js');



