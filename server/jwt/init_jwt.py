from flask_jwt import JWT, jwt_required, current_identity
from .utility import authenticate, identity,jwt_payload_callback

SECRET_KEY = 'usan'
jwt = JWT()
def init_jwt(app):

    jwt.authentication_handler(authenticate)
    jwt.identity_handler(identity)
    jwt.jwt_payload_callback(jwt_payload_callback)
    jwt.init_app(app) 
