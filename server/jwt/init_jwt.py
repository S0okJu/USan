from flask_jwt import JWT

jwt = JWT()
def init_jwt(app):
    global jwt 
    jwt.init_app(app)
