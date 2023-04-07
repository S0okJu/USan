import sys, os
from datetime import timedelta

from flask import Flask
from dotenv import load_dotenv

# custom 
from db.init_db import init_db
from resources import product, user,imgs
from jwt.init_jwt import init_jwt

# Blueprint
app = Flask(__name__)

# DB Setup 
load_dotenv()
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('SQLALCHEMY_DATABASE_URI')
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
init_db(app)

# File upload setup
app.config['ALLOWED_EXTENSIONS'] = set(['png', 'jpg', 'jpeg', 'gif'])

# JWT Setup
init_jwt(app)
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(minutes=30)  
app.config['JWT_REFRESH_TOKEN_EXPIRES'] = timedelta(days=30)  
app.config['JWT_BLACKLIST_ENABLED'] = True
app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access', 'refresh']
app.config['JWT_SECRET_KEY'] = 'usan#112'


# Register the blueprint 
app.register_blueprint(product.bp)
app.register_blueprint(user.bp)
app.register_blueprint(imgs.bp)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6000, debug=True)
    