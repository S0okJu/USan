from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate

rdb = SQLAlchemy()
migrate = Migrate()

def init_db(app):
    rdb.init_app(app)
    migrate.init_app(app, rdb)
