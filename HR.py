from flask import Flask, request
from flask_restful import Api, Resource, reqparse
import mysql.connector as mysqlConnector
from mysql.connector import Error
import json
import requests as rq

# import requests

app = Flask(__name__)
api = Api(app)


def create_db_connection(host_name, user_name, user_password, db_name):
    connection = None
    try:
        connection = mysqlConnector.connect(
            host=host_name,
            user=user_name,
            passwd=user_password,
            database=db_name
        )
        print("Connected")
    except Error as err:
        print(f"Error: '{err}'")
    return connection


def execute_query(connection, query):
    cursor = connection.cursor()
    try:
        cursor.execute(query)
        connection.commit()
        print("Query successful")
        return True
    except Error as err:
        print(f"Error '{err}'")
        return None


def read_query(connection, query):
    cursor = connection.cursor()
    result = None
    try:
        cursor.execute(query)
        result = cursor.fetchall()
        return result
    except Error as err:
        print(f"Error '{err}'")


def createArray(connection, query):
    results = read_query(connection, query)
    array = []
    for row in results:
        array.append({'id': row[0], 'fname': row[1], 'lname': row[2], 'age': row[3], 'gender': row[4]})
    connection.close()
    return array


# connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
# results = read_query(connection, q1)
# for row in results:
#     print(row)

@app.route('/users')
def getUsers():
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user"
    array = createArray(connection, query)
    return json.dumps({'users': array}, indent=4, ensure_ascii=True)


@app.route('/user/age', methods=['GET'])
def usersByAge():
    a = request.args.get('a')
    b = request.args.get('b')
    if int(a) > int(b):
        return "Wrong input", 400
    if int(a) < 0 or int(b) < 0:
        return "Wrong value", 400
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user WHERE age BETWEEN " + a + " AND " + b
    array = createArray(connection, query)
    return json.dumps({'count': len(array), 'users': array}, indent=4), 200


@app.route('/user', methods=['GET'])
def usersBy():
    if request.args.get('gender'):
        thisGend = request.args.get('gender')
        if thisGend != 'male' and thisGend != 'female' and thisGend != 'other':
            return "Wrong value", 400
        gender = 0 if thisGend == 'male' else 1 if thisGend == 'female' else 2
        connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
        query = "SELECT * FROM user WHERE gender = " + str(gender)
        array = createArray(connection, query)
        return json.dumps({'count': len(array), 'users': array}, indent=4), 200
    if request.args.get('pattern'):
        pattern = request.args.get('pattern')
        if pattern is None or pattern == "":
            return "Wrong pattern", 400
        query = f"SELECT * FROM user WHERE lname like '%{pattern}%' OR fname like '%{pattern}%'"
        connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
        array = createArray(connection, query)
        return json.dumps({'count': len(array), 'users': array}, indent=4), 200


def getUser(id):
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user WHERE id = " + str(id)
    results = read_query(connection, query)
    if not results:
        return False
    return True


@app.route('/user/<int:id>', methods=['GET', 'DELETE', 'PUT'])
def user(id):
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    if request.method == 'GET':
        query = f"SELECT * FROM user WHERE id = {id}"
        results = read_query(connection, query)
        if not results:
            return "This user does not exist", 404
        for row in results:
            return json.dumps({'id': row[0], 'fname': row[1], 'lname': row[2], 'age': row[3], 'gender': row[4]},
                              indent=4), 200
    if request.method == 'DELETE':
        if not getUser(id):
            return "This user does not exist", 404
        query = f"DELETE FROM user WHERE id = {id}"
        if execute_query(connection, query) is None:
            return "Error", 400
        return "User deleted", 200
    if request.method == 'PUT':
        if not getUser(id):
            return "This user does not exist", 404
        body = request.get_json()
        age = body.get('newage')
        if age < 0 or age > 100:
            return "Incorrect age", 400
        query = f"UPDATE user SET age = {age} WHERE id = {id}"
        if execute_query(connection, query) is None:
            return "Error", 400
        return f"Updated age for user id: {id}", 200





@app.route('/new', methods=['POST'])
def insertNewUser():
    body = request.get_json()
    fname = body.get('fname')
    if fname == "" or fname is None:
        return "Wrong first name", 400
    lname = body.get('lname')
    if lname == "" or lname is None:
        return "Wrong last name", 400
    age = body.get('age')
    if age > 100 or age < 0:
        return "Wrong age", 400
    gender = 2 if body.get('gender') is None else 1 if body.get('gender').lower() == 'female' else 0
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = f"INSERT INTO user(fname, lname, age, gender) VALUES('{fname}', '{lname}', {age}, {gender})"
    if execute_query(connection, query) is None:
        return "Error", 400
    return "User created", 201


@app.route('/', methods=['GET'])
def getDetails():
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user"
    array = createArray(connection, query)
    males = 0
    females = 0
    min = array[0]['age']
    max = array[0]['age']
    age = 0
    for person in array:
        age -= - person['age']
        if person['gender'] == 0:
            males -= -1
        elif person['gender'] == 1:
            females -= -1
        if person['age'] < min:
            min = person['age']
        if person['age'] > max:
            max = person['age']

    return json.dumps({'count': len(array),
                       'males': males,
                       'females': females,
                       'age': round(age/len(array), 2),
                       'min': min,
                       'max': max
                       }, indent=4), 200


if __name__ == '__main__':
    app.run(debug=True)
