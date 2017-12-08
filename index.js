var sockets;

function getTime(){
  var date = new Date();
  var hours = date.getHours();
  var minutes = date.getMinutes();
  return hours+':'+minutes;
}

function connect(){
    var protocol = (window.location.protocol == "https:" ? "wss://" : "ws://");
    var hosts = ["localhost:9000", "s55969da3.adsl.online.nl/ai/"];
    sockets = [new WebSocket(protocol + hosts[0]), new WebSocket(protocol + hosts[1])];
    sockets[0].onmessage = function(event){
      var message = '<div class="container"><img src="/w3images/bandmember.jpg" alt="AI"><p></p><span class="time-right">'+getTime()+'</span></div>';
      post(message, event.data);
    }
    sockets[1].onmessage = sockets[0].onmessage;
}

function post(message, data){
  var display = document.getElementById('display');
  display.innerHTML += message;
  display.lastChild.childNodes[1].innerText = data;
  display.scrollTop = display.scrollHeight;
}

function send(){
  var data = document.getElementById('message').value;
  String.prototype.isEmpty = function(){
    return (this.length === 0 || !this.trim());
  };
  if (!data.isEmpty()){
    if(sockets[0].readyState){
      sockets[0].send(data);
    }
    var message = '<div class="container"><img src="/w3images/bandmember.jpg" alt="Me" class="right"><p></p><span class="time-left">'+getTime()+'</span></div>';
    post(message, data);
  }
  document.getElementById('message').value = "";
}

function enter(e){
    if (e.keyCode == 13){
        send();
    }
}

window.onload = connect;
