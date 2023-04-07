import datetime 
from flask_jwt import JWT, current_app
from .utility import authenticate, identity

SECRET_KEY = 'usan#112'

jwt = JWT()

def init_jwt(app):
    jwt.authentication_handler(authenticate)
    jwt.identity_handler(identity)
    jwt.init_app(app) 
    
