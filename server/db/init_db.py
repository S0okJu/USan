from flask_sqlalchemy import SQLAlchemy

rdb = SQLAlchemy()

def init_db(app):
    rdb.init_app(app)