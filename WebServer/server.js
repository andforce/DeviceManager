var bodyParser = require('body-parser');

var express = require('express');
var app = express();
var server = require('http').createServer(app);

app.use(bodyParser());

const fs = require('fs');
const path = require("path");

const multer = require('multer');

// Set storage engine
const storage = multer.diskStorage({
    destination: './www/uploads/',
    filename: function(req, file, cb) {
        cb(null, file.fieldname + '-' + Date.now() + path.extname(file.originalname));
    }
});

// Init upload，不检查文件类型
const upload = multer({
    storage: storage
}).single('apk');

const io = require('socket.io')(server)
const sockets = {};

// Upload image
app.post('/upload', (req, res) => {
    upload(req, res, (err) => {
        if (err) {
            console.log(err);
            res.send('error');
        } else {
            console.log("upload success");
            console.log(req.file);
            res.send('success');

            // sockets 是一个对象，保存了所有的socket连接
            // 遍历sockets，发送appinfo事件
            for (var key in sockets) {
                console.log('emit apk-upload event');
                const apkInfo = new Object({
                    name: req.file.filename,
                    path: '/uploads/' + req.file.filename
                });
                sockets[key].broadcast.emit('apk-upload', apkInfo);
            }
        }
    });
});

app.post('/post_appinfo', (req, res) => {
    console.log('receive post appinfo event, req:' + req);

    // Get the data from the request body
    var data = req.body;
    // data 是list，循环打印
    data.forEach(function(item) {
        console.log('name: ' + item.name + ', packageName: ' + item.packageName);
    });

    // 把data 保存到apps.json文件
    const savePath = path.join(__dirname, "www", 'apps.json');
    fs.writeFile(savePath, JSON.stringify(data), function (err) {
        if (err) {
            console.log(err);
        } else {
            console.log('write apps.json success');
            // sockets 是一个对象，保存了所有的socket连接
            // 遍历sockets，发送appinfo事件
            for (var key in sockets) {
                sockets[key].emit('ACTION_APPINFO', data);
                console.log('emit appinfo event, error: no socket');
            }
        }
    });
    res.send({ code: 100, message: 'success' });
});


//specify the html we will use
app.use('/', express.static(__dirname + '/www'));

//bind the server to the 80 port
//server.listen(3000);//for local test
server.listen(process.env.PORT || 3001);//publish to heroku


io.sockets.on('connection', function(socket) {
    console.log('a user connected');
    sockets[socket.id] = socket;
    //user leaves
    socket.on('disconnect', function() {
        console.log('a user disconnected');
        delete sockets[socket.id];
    });

    // on mousedown
    socket.on('mouse-down', function(data) {
        console.log("received mousedown event, start emit mousedown event");
        socket.broadcast.emit('mouse-down', data);
    });
    // on mouseup
    socket.on('mouse-up', function(data) {
        console.log("received mouse-up event, start broadcast mouse-up event");
        socket.broadcast.emit('mouse-up', data);
    });
    // on mousemove
    socket.on('mouse-move', function(data) {
        console.log('mousemove event:', data);
        socket.broadcast.emit('mouse-move', data);
    });
    // apk upload
    socket.on('apk-upload', function(data) {
        console.log('apk-upload event:', data);
        socket.broadcast.emit('apk-upload', data);
    });

    socket.on('uninstall-app', function(data) {
        console.log('uninstall-app event:', data);
        socket.broadcast.emit('uninstall-app', data);
    });

    //new image get
    socket.on('image', function(imgData, color) {
        const savePath = path.join(__dirname, "www", 'screen.jpeg');
        fs.writeFile(savePath, imgData, function (err) {
            if (err) {
                console.log(err);
            } else {
                const pathWithTime = "screen.jpeg?t=" + new Date().getTime();
                console.log('image received, updateImage: ' + pathWithTime);
                socket.broadcast.emit('ACTION_UPDATE_IMAGE', pathWithTime, color);
            }
        });
    });
});
