from flask import Flask, request, jsonify, redirect, url_for
import jwt
import bcrypt
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime, timedelta
from werkzeug import generate_password_hash, check_password_hash
import hashlib
from flask_jwt_extended import get_jti
from config import db

app = Flask(__name__)

# [회원가입 API]
# id, pw, nickname을 받아서, mongoDB에 저장합니다.
# 저장하기 전에, pw를 sha256 방법(=단방향 암호화. 풀어볼 수 없음)으로 암호화해서 저장합니다.
@app.route('/auth/register', methods=['POST'])
def api_register():
    user_info = request.get_json()
    if 
    id_receive = user_info['id_give']
    pw_receive = user_info['pw_give']
    nickname_receive = request.form['nickname_give']
    eamil_receive = request.form['email_give']

    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()

    db.user.insert_one({'id': id_receive, 'pw': pw_hash, 'nick': nickname_receive, 'email':email_receive})

    return jsonify({'result': 'success'})


# id, pw를 받아서 맞춰보고, 토큰을 만들어 발급합니다.
@app.route('/auth/login', methods=['POST'])
def api_login():
    id_receive = request.form['id_give']
    pw_receive = request.form['pw_give']

    # 회원가입 때와 같은 방법으로 pw를 암호화합니다.
    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()

    # id, 암호화된pw을 가지고 해당 유저를 찾습니다.
    result = db.user.find_one({'id': id_receive, 'pw': pw_hash})

    # 찾으면 JWT 토큰을 만들어 발급합니다.
    if result is not None:
        # JWT 토큰에는, payload와 시크릿키가 필요합니다.
        # 시크릿키가 있어야 토큰을 디코딩(=풀기) 해서 payload 값을 볼 수 있습니다.
        # 아래에선 id와 exp를 담았습니다. 즉, JWT 토큰을 풀면 유저ID 값을 알 수 있습니다.
        # exp에는 만료시간을 넣어줍니다. 만료시간이 지나면, 시크릿키로 토큰을 풀 때 만료되었다고 에러가 납니다.
        payload = {
            'id': id_receive,
            'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds=5)
        }
        token = jwt.encode(payload, SECRET_KEY, algorithm='HS256').decode('utf-8')

        # token을 줍니다.
        return jsonify({'result': 'success', 'token': token})
    # 찾지 못하면
    else:
        return jsonify({'result': 'fail', 'msg': '아이디/비밀번호가 일치하지 않습니다.'})

@app.route('/')
def home():
    token_receive = request.cookies.get('mytoken')
    try:
        payload = jwt.decode(token_receive, SECRET_KEY, algorithms=['HS256'])
        user_info = db.user.find_one({"id": payload['id']})
        return redirect(url_for('login sucess'))
    except jwt.ExpiredSignatureError:
        return redirect(url_for("login-time out"))
    except jwt.exceptions.DecodeError:
        return redirect(url_for("login-login infomation not define"))

    jwt_blocklist = set()  ## 로그아웃을 위한.

    class UserLogoutResource(Resource):

        @jwt_required()
        def post(selt):
            jti = get_jwt()['jti']
            jwt_blocklist.add(jti)

            return {'message': 'Log Out'}, HTTPStatus.OK


    if __name__ == "__main__":
        app.run()


