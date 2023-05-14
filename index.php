<!DOCTYPE html>
<html>
<head>
  <title>Location Data</title>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/socket.io/4.4.1/socket.io.js"></script>
</head>
<body>
  <h1>Location Data</h1>
  <div>
    <label for="room">Room:</label>
    <input type="text" id="room">
  </div>
  <div>
    <label for="username">Username:</label>
    <input type="text" id="username">
  </div>
  <div>
    <label for="latitude">Latitude:</label>
    <input type="text" id="latitude">
  </div>
  <div>
    <label for="longitude">Longitude:</label>
    <input type="text" id="longitude">
  </div>
  <div>
    <label for="role">Role:</label>
    <input type="text" id="role">
  </div>
  <button onclick="sendLocationData()">Send</button>

  <script>
    // 서버에 연결
    const socket = io.connect('http://localhost:6000');
    socket.on("connect", function(){
      socket.emit("location_data", document.getElementById('room').value);
    })
    // // location_data 이벤트를 수신하여 데이터 출력
    // socket.on('location_data', (data) => {
    //   console.log('Received location data:', data);
    // });

    // 위치 데이터 전송
    function sendLocationData() {
      const room = document.getElementById('room').value;
      const username = document.getElementById('username').value;
      const latitude = parseFloat(document.getElementById('latitude').value);
      const longitude = parseFloat(document.getElementById('longitude').value);
      const role = document.getElementById('role').value;

      // // 데이터 객체 생성
      // const data = {
      //   room: room,
      //   username: username,
      //   location: { latitude: latitude, longitude: longitude },
      //   role: role
      // };

      // // 서버로 데이터 전송
      // socket.emit('location_data', data);
    }
  </script>
</body>
</html>
