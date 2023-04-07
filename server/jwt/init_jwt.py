from flask_jwt import JWT, jwt_required, current_identity
from .utility import authenticate, identity

SECRET_KEY = 'usan'
jwt = None
def init_jwt(app):
    global jwt
    jwt = JWT(app, authenticate,identity)
