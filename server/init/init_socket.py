from flask_socketio import SocketIO
 
socketio = None 

def init_socket(app):
    global socketio
    socketio = SocketIO(app)