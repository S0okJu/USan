import os, sys
import json
from collections import defaultdict

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc
from flask_socketio import SocketIO, Namespace, emit
from geopy.distance import geodesic

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
from init.init_socket import socketio
import utils.error.custom_error as error

bp = Blueprint('location', '/location')


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
@jwt_required()
def make_room(product_id):
    room = product_id
    r.add(room)
    print(f'Make {room} room completely!')    
    product_s = ProductModel.query.get(int(product_id))
    if not product_s:
        return jsonify({"message":"Not found"}), 404
    # get destination data 
    dest = {
        "latitude" : product_s.latitude,
        "longitude": product_s.longitude
    }
    return jsonify(dest), 200


@socketio.on('location_data')
def handle_location_data(data):
    room = int(data['room'])
    username = data['username']
    location = data['address']
    role = int(data['role'])

    # 위치 정보를 업데이트하고 결과 데이터 생성
    if role == 0:
        rooms[room]['buyer'] = {
            'username': username,
            'address': location
        }
    elif role == 1:
        rooms[room]['seller'] = {
            'username': username,
            'address': location
        }
    
    # 모든 위치 데이터가 도착하면 통합 데이터를 생성하여 전송
    if rooms[room]['buyer'] is not None and rooms[room]['seller'] is not None:
        buyer_location = rooms[room]['buyer']['address']
        seller_location = rooms[room]['seller']['address']

        # 거리를 계산합니다.
        distance = geodesic(buyer_location, seller_location).miles

        # 거리 차가 5 이상이면 방을 삭제하고 끝났음을 알립니다.
        if abs(distance) >= 5:
            del rooms[room]
            emit('end', {'message': 'The distance difference is greater than or equal to 5. The room has been deleted.'}, room=room)
        else:
            integrated_data = {
                'room': room,
                'buyer': rooms[room]['buyer'],
                'seller': rooms[room]['seller'],
            }
            emit('integrated_data', integrated_data, broadcast=True, room=room)


@bp.route("/<int:product_id>/finish",methods=["GET"])
def finish_room(product_id):
    room_name = product_id
    socketio.leave_room(room_name)
    rooms.discard(room_name)
    print(f'Room "{room_name}" removed')
    
    return jsonify({"msg":"Success"}), 200 
    

