import bcrypt
import jwt
#### JWT ####
app.config['JWT_SECRET_KEY'] = 'usan'
algorithm = 'HS256'

# ...

class User():
    def login(email, input_password):
        # bcrypt hash transfer
        password_bytes = input_password.encode('utf-8')
        # MySQL DB에 해당 계정 정보가 있는지 확인
        cursor = mysql.connection.cursor(MySQLdb.cursors.DictCursor)
        cursor.execute('SELECT * FROM users WHERE email = %s', [email])
        # 값이 유무 확인 결과값 account 변수로 넣기
        account = cursor.fetchone()
        if account:
            db_password_bytes = account['hashed_password'].encode('utf-8')
            check_password = bcrypt.checkpw(password_bytes, db_password_bytes)
            payload = {
                'email': account['email'],
                'hashed_password': account['hashed_password']
            }
            jwt_token = jwt.encode(payload, app.config['JWT_SECRET_KEY'], algorithm)
            return check_password, jwt_token.decode('utf-8')
        return False, False