db = {
    'user'     : 'root',
    'password' : '[mysql 비밀번호]',
    'host'     : '127.0.0.1',
    'port'     : '5000',
    'database' : 'flask_test'
}

DB_URL = f"mysql+mysqlconnector://{db['user']}:{db['password']}@{db['host']}:{db['port']}/{db['database']}?charset=utf8"