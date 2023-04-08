from flask import Flask, jsonify, request
from flask_jwt_extended import JWTManager, jwt_required, create_access_token, get_jwt_identity
from werkzeug.security import generate_password_hash, check_password_hash

# 임시 사용자 데이터베이스
users = [
    {
        'id': 1,
        'username': 'admin',
        'password': generate_password_hash('admin')
    }
]

app = Flask(__name__)
app.config['JWT_SECRET_KEY'] = 'super-secret'  # JWT 시크릿 키

jwt = JWTManager(app)

# JWT 토큰 생성 함수
def authenticate(username, password):
    user = next(filter(lambda u: u['username'] == username, users), None)
    if user and check_password_hash(user['password'], password):
        return user
    return None

# 회원가입 API 엔드포인트
@app.route('/signup', methods=['POST'])
def signup():
    data = request.get_json()

    if next(filter(lambda u: u['username'] == data['username'], users), None) is not None:
        return jsonify({'message': 'Username already exists'}), 400

    user = {
        'id': len(users) + 1,
        'username': data['username'],
        'password': generate_password_hash(data['password'])
    }

    users.append(user)

    return jsonify({'message': 'User created successfully'}), 201

# 로그인 API 엔드포인트
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username', None)
    password = data.get('password', None)

    if not username or not password:
        return jsonify({'message': 'Invalid username or password'}), 401

    user = authenticate(username, password)

    if not user:
        return jsonify({'message': 'Invalid username or password'}), 401

    access_token = create_access_token(identity=user['id'])
    return jsonify({'access_token': access_token}), 200

# 로그아웃 API 엔드포인트
@app.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    # JWT 토큰 블랙리스트 추가 등의 추가적인 작업이 필요한 경우 이곳에서 수행할 수 있습니다.
    return jsonify({'message': 'Logout successful'}), 200

# 로그인 필요 API 엔드포인트
@app.route('/protected')
@jwt_required()
def protected():
    current_user_id = get_jwt_identity()
    return jsonify({'message': f'Hello user {current_user_id}!'}), 200

if __name__ == '__main__':
    app.run(debug=True)