from flask import Flask, request
from flask_restful import Api, Resource, reqparse
import json
import methods

app = Flask(__name__)
api = Api(app)


@app.route('/users')
def getUsers():
    """
    Method that will return all users

    Returns: json format of all users
    """

    # connection to database
    connection = methods.create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    # query argument
    query = "SELECT * FROM user"
    # create array
    array = methods.createArray(connection, query)
    # return json with users
    return json.dumps({'users': array}, indent=4, ensure_ascii=False)


@app.route('/user/age', methods=['GET'])
def usersByAge():
    """
    Method that will return users by age
    Giving parameters to url, method will get them
    and search in database

    Return: json format with length of array and users
    """

    a = request.args.get('from')
    b = request.args.get('to')
    if int(a) > int(b):
        return "Wrong input", 400
    if int(a) < 0 or int(b) < 0:
        return "Wrong value", 400
    connection = methods.create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user WHERE age BETWEEN " + a + " AND " + b
    array = methods.createArray(connection, query)
    return json.dumps({'count': len(array), 'users': array}, indent=4, ensure_ascii=False), 200


@app.route('/user', methods=['GET'])
def usersBy():
    """
    Method that will return users based on criteria

    Returns: json format with users
    """

    connection = methods.create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    if request.args.get('gender'):
        """
        If parameter is gender, method will connect to database,
        search for every user with specified gender and return
        list of users
        
        Returns: json format with users
        """

        thisGend = request.args.get('gender')
        if thisGend != 'male' and thisGend != 'female' and thisGend != 'other':
            return "Wrong value", 400
        gender = 0 if thisGend == 'male' else 1 if thisGend == 'female' else 2
        query = "SELECT * FROM user WHERE gender = " + str(gender)
        array = methods.createArray(connection, query)
        return json.dumps({'count': len(array), 'users': array}, indent=4), 200
    if request.args.get('pattern'):
        """
        If parameter is pattern, method will connect to database,
        search for every user with specified pattern in first or
        last name and return list of users
        
        Returns: json format with users
        """

        pattern = request.args.get('pattern')
        if pattern is None or pattern == "":
            return "Wrong pattern", 400
        query = f"SELECT * FROM user WHERE lname like '%{pattern}%' OR fname like '%{pattern}%'"
        array = methods.createArray(connection, query)
        return json.dumps({'count': len(array), 'users': array}, indent=4, ensure_ascii=False), 200


@app.route('/user/<int:id>', methods=['GET', 'DELETE', 'PUT'])
def user(id):
    """
    This method will require id and will do specific action,
    according to method selected
    """

    connection = methods.create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = f"DELETE FROM user WHERE id = {id}"
    if request.method == 'GET':
        """
        If method is 'GET', this method will find user with
        specified id and return him
        
        If user does not exist, will return code 404
        
        Returns: json format with user
        """

        results = methods.read_query(connection, query)
        if not results:
            return "This user does not exist", 404
        for row in results:
            return json.dumps({'id': row[0], 'fname': row[1], 'lname': row[2], 'age': row[3], 'gender': row[4]},
                              indent=4), 200
    if request.method == 'DELETE':
        """
        If method is 'DELETE', this method will find user
        with specified id and delete him from database
        
        If user does not exist, will return code 404
        
        Returns: code 200 (if deleted) + string
        """

        if not methods.getUser(id):
            return "This user does not exist", 404
        if methods.execute_query(connection, query) is None:
            return "Error", 400
        return "User deleted", 200
    if request.method == 'PUT':
        """
        If method is 'PUT', this method will find user
        with specified id, get body content (req json 
        format with "newage": value) and change user's
        age
        
        If age value is greater than 100 or lower than 0,
        will return code 400
        
        If user does not exist, will return code 404
        
        Returns: string saying that age for user <id> 
        was updated + code 200
        """

        if not methods.getUser(id):
            return "This user does not exist", 404
        body = request.get_json()
        age = body.get('newage')
        if age < 0 or age > 100:
            return "Incorrect age", 400
        query = f"UPDATE user SET age = {age} WHERE id = {id}"
        if methods.execute_query(connection, query) is None:
            return "Error", 400
        return f"Updated age for user id: {id}", 200


@app.route('/new', methods=['POST'])
def insertNewUser():
    """
    Method that creates new user in database

    Required json format body with "fname", "lname",
    "age" (mandatory) and "gender" (default-value: 2)

    If "fname" or "lname" is missing or is empty, will
    return code 400
    If "age" is missing, or greater than 100, or lower
    than 0, will return code 400

    Returns: code 201 (if created) + string
    """

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
    connection = methods.create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = f"INSERT INTO user(fname, lname, age, gender) VALUES('{fname}', '{lname}', {age}, {gender})"
    if methods.execute_query(connection, query) is None:
        return "Error", 400
    return "User created", 201


@app.route('/', methods=['GET'])
def getDetails():
    """
    Method that returns details about database

    Returns: json format with:
        "count": number of users in database,
        "males": number of users with gender 'male',
        "females": number of users with gender 'female',
        "age": average age of all users,
        "min": minimum value of age,
        "max": maximum value of age
    """

    methods.getDetails()
    males = methods.details['males']
    females = methods.details['females']
    min = methods.details['min']
    max = methods.details['max']
    age = methods.details['age']
    length = methods.details['length']
    return json.dumps({'count': length,
                       'males': males,
                       'females': females,
                       'age': round(age / length, 2),
                       'min': min,
                       'max': max
                       }, indent=4), 200


if __name__ == '__main__':
    app.run(debug=True)
