import os, sys
import json
import uuid 

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc
from sqlalchemy import and_
from sqlalchemy.orm import joinedload

from stream_chat import StreamChat

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel, FavoriteModel, TransactionModel
import utils.error.custom_error as error
from init.init_db import rdb

bp = Blueprint('chat', __name__, url_prefix='/chat')

# git clone https://github.com/GetStream/android-chat-tutorial.git
# Stream Chat API 설정
api_key = 'dys2han9ecrs'
api_secret = '5xuybda2mxpvm4debjdf5fqfempj65yvagdpevdekknedmnh2ywd28gyva6uw2mu'

client = StreamChat(api_key, api_secret)

@bp.route('/start_chat/<int:product_id>/<string:username>', methods=['POST'])
def start_chat(product_id, username):

    # 채팅 정보 저장 
    product_session = ProductModel.query.filter_by(product_id=product_id).first()
    if not product_session:
        raise error.DBNotFound()
    
    user_session = UserModel.query.filter_by(username=username).first()
    if not user_session:
        raise error.DBNotFound()
    
    transaction_s = TransactionModel(product=product_session).first()
    if transaction_s: # 거래 목록이 존재한다면
        if product_session.author.username == username: # 판매자라면
           transaction_s.seller = user_session 
        else:
            transaction_s.buyer = user_session
        channel_id = transaction_s.channel_id
    else: 
        channel_id = uuid.uuid4()
        if product_session.author.username == username:
            trans_s = TransactionModel(product=product_session, seller=user_session, channel_id=channel_id)
        else:
            trans_s = TransactionModel(product=product_session, buyer=user_session, channel_id=channel_id)
        
        rdb.session.add(trans_s)
        
    rdb.commit()
    
    # Create Channel and add members 
    token = client.create_token(username)
    trans_session = TransactionModel(product=product_session).first()
    
    channel = client.channel("messaging", channel_id)
    channel.create(user_session.username)

    channel.add_members([trans_session.seller.username, trans_session.buyer.username])

    return jsonify({ 'channel_id': str(channel_id), 'chat_token': token}), 200 

@bp.route('/<string:channel_id>/message', methods=['POST'])
# @jwt_required()
def send_message(channel_id):
    user_id = get_jwt_identity()
    message_text = request.json['message']

    # 채널 선택
    channel_type = 'messaging'
    channel = client.channel(channel_type, channel_id=str(channel_id))

    # 메시지 전송
    response = channel.send_message({
        "text": message_text,
        "user": {"id": user_id}
    })

    return jsonify(response), 200 