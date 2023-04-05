from flask_jwt_extended import JWTManager

jwt = None
SECRET_KEY = 'usan'

def init_jwt(app):
    if jwt is None:
        jwt = JWTManager(app)
    