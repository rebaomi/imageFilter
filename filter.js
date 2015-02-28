/*
 * @description 处理图片滤镜
 * @return 滤镜图片
 * @author lvxd
 */
var fs = require("fs")
    ,url = require('url')
    ,path = require('path')
    ,request = require('request')
    ,mine = require('./alloyImage_module/util/mine').types
    ,config = require('./alloyImage_module/util/config').config

    ,base = require('./alloyImage_module/base.js')
    ,$AI = base.psLib
    ,Canvas = require('canvas')
    ,Image = Canvas.Image
    ,debug = require('debug')('imageFilter:server')
    ,port = normalizePort(process.env.PORT || '8000')
    ,cluster = require('cluster')
    ,http = require('http')
    ,async = require('async')
    ,domain = require('domain')
    ,numCPUs = require('os').cpus().length;

http.globalAgent.maxSockets = 40000;

if (cluster.isMaster) {
    console.log('[master] ' + "start master...");

    for (var i = 0; i < numCPUs; i++) {
        cluster.fork();
    }

    cluster.on('listening', function (worker, address) {
        console.log('[master] ' + 'listening: worker' + worker.id + ',pid:' + worker.process.pid + ', Address:' + address.address + ":" + address.port);
    });

} else if (cluster.isWorker) {
    console.log('[worker] ' + "start worker ..." + cluster.worker.id);

    var server = http.createServer(function(req,res){
        var pathname = url.parse(req.url).pathname
            , params = url.parse(req.url,true).query
            , baseUrl = config.getBaseUrl(params.ossServer)
            , imgUrl = config.getImgUrl(pathname, params,baseUrl)

            , effect = params.effect || 'origin'
//            , args = params.args || 0
            , base64Img, img, imgWidth, imgHeight
            , canvas, ctx, result, dataBuffer;

        // 开始请求图片并进行滤镜处理
        console.log("["+new Date()+"]" + imgUrl);
        var reqDomain = request({url:imgUrl, encoding:null}, callback);

        // 异常监听和处理
        var errorHandler = domain.create();
        errorHandler.add(reqDomain);
        errorHandler.on('error',function(err){
            console.log("[ERROR] ["+new Date()+"]" + imgUrl + "\n" + err);
            fs.readFile("./public/images/sys-pic.png", function(err, imgSquid){
                res.writeHead(200,
                    {'Content-Type':'image/png',
                        'Cache-Control':'max-age=3600'});
                res.end(imgSquid);
            });

        });

//        errorHandler.run(function(){
//        });

        // 回调函数 处理图片滤镜
        function callback(error,response,body){
            if (error) throw error;

            img = new Image;
            img.src = new Buffer(body, 'binary');

            if('origin' === effect){
                res.writeHead(200,
                    {'Content-Type':'text/plain',
                        'Cache-Control':'max-age=3600'});
                res.end(new Buffer(body, 'binary'));
            }

            imgWidth = img.width || 1500;
            imgHeight = img.height || 1500;

            canvas = new Canvas(imgWidth, imgHeight);
            ctx = canvas.getContext("2d");

            result = $AI(img).ps(effect);
//            result = $AI(img).act("高斯模糊",6);

            ctx.putImageData(result.imgData, 0, 0);
            base64Img  = canvas.toDataURL().replace(/^data:image\/\w+;base64,/, "");

            res.writeHead(200,
                {'Content-Type':'image/png',
                    'Cache-Control':'max-age=3600'});

            dataBuffer = new Buffer(base64Img, 'base64');
            console.log('[worker]current worker'+cluster.worker.id);
            res.end(dataBuffer);
        }
    });

    server.listen(port);
    server.on('error', onError);
    server.on('listening', onListening);

    function normalizePort(val) {
        var port = parseInt(val, 10);

        if (isNaN(port)) {
            // named pipe
            return val;
        }

        if (port >= 0) {
            // port number
            return port;
        }

        return false;
    }

    function onError(error) {
        if (error.syscall !== 'listen') {
            throw error;
        }

        var bind = typeof port === 'string'
            ? 'Pipe ' + port
            : 'Port ' + port

        // handle specific listen errors with friendly messages
        switch (error.code) {
            case 'EACCES':
                console.error(bind + ' requires elevated privileges');
                process.exit(1);
                break;
            case 'EADDRINUSE':
                console.error(bind + ' is already in use');
                process.exit(1);
                break;
            default:
                throw error;
        }
    }

    function onListening() {
        var addr = server.address();
        var bind = typeof addr === 'string'
            ? 'pipe ' + addr
            : 'port ' + addr.port;
        debug('Listening on ' + bind);
    }

}


