from flask_jwt import JWT, jwt_required, current_identity
from .utility import authenticate, identity

SECRET_KEY = 'usan'
jwt = JWT()
def init_jwt(app):

    jwt.authentication_handler(authenticate)
    jwt.identity_handler(identity)
    jwt.init_app(app) 
