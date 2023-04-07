import os, sys
import datetime
import json 
import hashlib

from flask import request,Blueprint, Response
from flask_jwt import JWT, jwt_required, current_identity

# custom 
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import UserModel
from db.init_db import rdb
from jwt.init_jwt import jwt
from jwt.utility import authenticate

bp = Blueprint('user', __name__, url_prefix='/user')
blacklist = set()

# 로그인 API 엔드포인트
@bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()

    if not data['email'] or not data['password']:
        return json.dumps({'message': '인증 실패!'}), 401

    user = authenticate(data['email'], data['password'])

    if not user:
        return json.dumps({'message': '인증 실패!'}), 401

    access_token = jwt.jwt_encode_callback({'identity': user['email']})
    return json.dumps({'access_token': access_token.decode('utf-8')}), 200

# 로그아웃 API 엔드포인트
@bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    jti = current_identity.get('jti')
    blacklist.add(jti)
    return json.dumps({'message': '로그아웃 성공!'}), 200

# 로그인 필요 API 엔드포인트
@bp.route('/protected')
@jwt_required()
def protected():
    return json.dumps({'message': '인증 성공!'})

@bp.route('/signup', methods=['POST'])
def signup():
    # 클라이언트로부터 받은 데이터 가져오기
    user = request.get_json()
    session = UserModel(username=user['username'], email=user['email'],
        password=user['password'], created_date= datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        modified_date=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    rdb.session.add(session)
    rdb.session.commit()
    
    result = {'message': 'success Signup!!'}
    return json.dumps(result)













# # TODO JWT Token DB에 저장하기 
# @jwt.token_in_blacklist_loader
# def check_if_token_in_blacklist(decrypted_token):
#     jti = decrypted_token['jti']
#     return jti in blacklist

# @bp.route('/register', methods=['POST'])
# def api_register():
#     user_info = request.get_json()
#     if user_info:
#         pass 

#     pw_receive = user_info['pw']
#     nickname_receive = user_info['nickname']
#     email_receive = user_info['email']

#     pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()
#     user = UserModel(username=nickname_receive, email=email_receive, password=pw_hash)
#     rdb.session.add(user)
#     rdb.commit()
#     # db.user.insert_one({'id': id_receive, 'pw': pw_hash, 'nick': nickname_receive, 'email':email_receive})

#     return Response(
#         response = json.dumps({"msg":"User created successfully"}, ensure_ascii=False),
#         status=201,
#         mimetype="application/json" 
#     )


# @bp.route('/login', methods=["POST"])
# def api_login():
#     user_info = request.get_json()
#     email_receive = user_info['email']
#     pw_receive = user_info['pw']

#     # 회원가입 때와 같은 방법으로 pw를 암호화합니다.
#     pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()

#     # id, 암호화된pw을 가지고 해당 유저를 찾습니다.
#     result = UserModel.query.find_one({'email': email_receive, 'pw': pw_hash})

#     # 찾으면 JWT 토큰을 만들어 발급합니다.
#     if result is not None:
#         # JWT 토큰에는, payload와 시크릿키가 필요합니다.
#         # 시크릿키가 있어야 토큰을 디코딩(=풀기) 해서 payload 값을 볼 수 있습니다.
#         # 아래에선 id와 exp를 담았습니다. 즉, JWT 토큰을 풀면 유저ID 값을 알 수 있습니다.
#         # exp에는 만료시간을 넣어줍니다. 만료시간이 지나면, 시크릿키로 토큰을 풀 때 만료되었다고 에러가 납니다.

#         # * Create Access, Refresh token
#         access_token = create_access_token(identity=email_receive)
#         refresh_token = create_refresh_token(identity= email_receive)
        
#         # token을 줍니다.
#         return Response(
#             response = json.dumps({'msg': 'Login in successfully', 'access_token': access_token, 'refresh_token':refresh_token}),
#             status=200,
#             mimetype="application/json")
#     # 찾지 못하면
#     else:
#         return Response(
#             Response = json.dumps({'result': 'fail', 'msg': '아이디/비밀번호가 일치하지 않습니다.'},ensure_ascii=False),
#             status=401,
#             mimetype="application/json"
#         ) 

# @bp.route('/logout', methods=['POST'])
# @jwt_required
# def logout():
#     # Get JWT ID of the access token 
#     jti = get_jwt_identity()['jti']
#     blacklist.add(jti)

#     return Response(json.dumps({'msg': 'Logged out successfully'}), status=200, mimetype='application/json')


# @bp.route('/refresh', methods=["POST"])
# @jwt_required 
# def refresh():
#     current_user = get_jwt_identity()
#     access_token = create_access_token(identity=current_user)
#     resp = {'access_token': access_token}
#     return json.dumps(resp) 


# @bp.route('/protected', methods=["GET"])
# @jwt_required 
# def protected():
#     user_email = get_jwt_identity()
#     # user = UserModel.query.filter_by(email=user_email).first()
#     return json.dumps({'msg': f'hello {user_email}'})