import datetime 
from flask_jwt_extended import JWTManager

SECRET_KEY = 'usan#112'

jwt = JWTManager()

def init_jwt(app):
    jwt.init_app(app)