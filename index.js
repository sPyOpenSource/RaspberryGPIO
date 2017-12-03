var socket;

function getTime(){
  var d = new Date();
  var h = d.getHours();
  var m = d.getMinutes();
  return h+':'+m;
}

function connect() {
    var host = (window.location.protocol == "https:"
        ? "wss://" : "ws://") + "localhost:9000";
    socket = new WebSocket(host);
    socket.onmessage = function(event) {
      var message = '<div class="container"><img src="/w3images/bandmember.jpg" alt="AI"><p>'+event.data+'</p><span class="time-right">'+getTime()+'</span></div>';
      document.getElementById('display').innerHTML += message;
      document.getElementById('display').scrollTop = document.getElementById('display').scrollHeight;
    }
}

function send(){
  var data = document.getElementById('message').value;
  if (data===""){
    return;
  }
  socket.send(data);
  var message = '<div class="container"><img src="/w3images/bandmember.jpg" alt="Me" class="right"><p>'+data+'</p><span class="time-left">'+getTime()+'</span></div>';
  document.getElementById('display').innerHTML += message;
  document.getElementById('display').scrollTop = document.getElementById('display').scrollHeight;
  document.getElementById('message').value = "";
}

function enter(e) {
    if (e.keyCode == 13) {
        send();
    }
}

window.onload = connect;
