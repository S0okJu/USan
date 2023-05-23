import os, sys
import json

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc
from sqlalchemy import and_
from sqlalchemy.orm import joinedload

from stream_chat import StreamChat

bp = Blueprint('chat', __name__, url_prefix='/chat')


# Stream Chat API 설정
api_key = 'xw82948gephd'
api_secret = 's7cb46tuzbtj7ew5xt5msancg6rsevgfyrft27yvdusrzuxqygdpd8a3gqr29r65'

client = StreamChat(api_key, api_secret)

@bp.route('/start_chat/<string:username>', methods=['POST'])
def start_chat(username):

    # Stream Chat API와 연결 및 토큰 발급
    token = client.create_token(username)

    # 채널 생성 및 사용자 추가
    channel_type = 'messaging'
    channel = client.channel(channel_type, channel_id=1)
    channel.create(username)

    return jsonify({ 'channel_id': '1', 'token': token})

@bp.route('/send_message', methods=['POST'])
def send_message():
    user_id = request.json['user_id']
    channel_id = request.json['channel_id']
    message_text = request.json['message']

    # 채널 선택
    channel_type = 'messaging'
    channel = client.channel(channel_type, channel_id=channel_id)

    # 메시지 전송
    response = channel.send_message({
        "text": message_text,
        "user": {"id": user_id}
    })

    return jsonify(response)