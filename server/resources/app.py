from datetime import timedelta
from flask import Flask, jsonify, request
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from werkzeug.security import generate_password_hash, check_password_hash

# 임시 사용자 데이터베이스
users = [
    {
        'id': 1,
        'username': 'admin',
        'email': 'admin@example.com',
        'password': generate_password_hash('admin', method='sha256')
    }
]

# Flask 애플리케이션 설정
app = Flask(__name__)
app.config['SECRET_KEY'] = 'super-secret'  # JWT 시크릿 키
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(minutes=30)  # JWT 토큰 만료 시간 설정 (30분)

jwt = JWTManager(app)

# 회원가입 API 엔드포인트
@app.route('/signup', methods=['POST'])
def signup():
    data = request.get_json()

    # 사용자 이름이 이미 존재하는 경우 에러 메시지 반환
    if any(user['username'] == data['username'] for user in users):
        return jsonify({'message': 'Username already exists'}), 400

    # 이메일이 이미 존재하는 경우 에러 메시지 반환
    if any(user['email'] == data['email'] for user in users):
        return jsonify({'message': 'Email already exists'}), 400

    # 비밀번호를 해싱해서 저장
    hashed_password = generate_password_hash(data['password'], method='sha256')

    # 사용자 데이터베이스에 추가
    user = {
        'id': len(users) + 1,
        'username': data['username'],
        'email': data['email'],
        'password': hashed_password
    }
    users.append(user)

    return jsonify({'message': 'User created successfully'}), 201


# 로그인 API 엔드포인트
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username', None)
    password = data.get('password', None)

    # 입력값이 없는 경우 에러 메시지 반환
    if not username or not password:
        return jsonify({'message': 'Missing username or password'}), 400

    # 사용자 이름으로 사용자 찾기
    user = next((user for user in users if user['username'] == username), None)

    # 사용자가 없거나 비밀번호가 일치하지 않는 경우 에러 메시지 반환
    if not user or not check_password_hash(user['password'], password):
        return jsonify({'message': 'Invalid username or password'}), 401

    # JWT 토큰 생성해서 반환
    access_token = create_access_token(identity=user['id'])
    return jsonify({'access_token': access_token}), 200


# 로그아웃 API 엔드포인트
@app.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    # JWT 토큰 블랙리스트에 추가
    jti = get_jwt_identity()
    jwt.blocklist