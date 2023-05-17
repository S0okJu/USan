import os, sys
import json
from collections import defaultdict

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc
from flask_socketio import SocketIO, Namespace, emit

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
from init.init_socket import socketio
import utils.error.custom_error as error

bp = Blueprint('location', __name__)


@bp.route("/<string:username>", methods=["POST"])
def post_location(username):

    user = UserModel.check_by_username(username)
    if user == False:
        raise error.DBNotFound("User")
    
    body = request.get_json()
    if not body:
        raise error.EmptyJSONError()

    
@socketio.on("connect")
def handle_connect():
    print("Client Conenct!")
r = set()    
rooms = defaultdict(lambda: {'buyer': None, 'seller': None})

@bp.route("/<int:product_id>/start", methods=["GET"])
def make_room(product_id):
    room = product_id
    r.add(room)
    print(f'Make {room} room completely!')    

    return jsonify({"msg":"Success"}), 200

@socketio.on('location_data')
def handle_location_data(data):
    room = int(data['room'])
    username = data['username']
    location = data['location']
    role = int(data['role'])

    # 위치 정보를 업데이트하고 결과 데이터 생성
    if role == 0:
        rooms[room]['buyer'] = {
            'username': username,
            'location': location
        }
    elif role == 1:
        rooms[room]['seller'] = {
            'username': username,
            'location': location
        }

    # 모든 위치 데이터가 도착하면 통합 데이터를 생성하여 전송
    if rooms[room]['buyer'] is not None and rooms[room]['seller'] is not None:
        integrated_data = {
            'room': room,
            'buyer': rooms[room]['buyer'],
            'seller': rooms[room]['seller'],
        }
        emit('integrated_data', integrated_data, broadcast=True)



@bp.route("/<int:product_id>/finish",methods=["GET"])
def finish_room(product_id):
    room_name = product_id
    socketio.leave_room(room_name)
    rooms.discard(room_name)
    print(f'Room "{room_name}" removed')
    
    return jsonify({"msg":"Success"}), 200 
    

