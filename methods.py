import mysql.connector as mysqlConnector
from mysql.connector import Error


def getUser(id):
    """
    Method that searches for user in database

    If id is lower than 0 or if user is not found,
    will return False

    Returns: True (if user is found)
             False (if user is not found or if < 0)
    """

    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user WHERE id = " + str(id)
    results = read_query(connection, query)
    if not results or id < 0:
        return False
    return True


def create_db_connection(host_name, user_name, user_password, db_name):
    """
    Method that connects to database

    If error occurs, will return error

    Returns: connection to database
    """

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
    """
    Method that will execute provided query

    Returns: True (if query was executed)
             None (if error occurred)
    """

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
    """
    Method that will execute "SELECT" query

    Returns: result from query (list)
    """

    cursor = connection.cursor()
    result = None
    try:
        cursor.execute(query)
        result = cursor.fetchall()
        return result
    except Error as err:
        print(f"Error '{err}'")


def createArray(connection, query):
    """
    Method that will create array from specific query

    Returns: custom array with users
    """

    results = read_query(connection, query)
    array = []
    for row in results:
        array.append({'id': row[0], 'fname': row[1], 'lname': row[2], 'age': row[3], 'gender': row[4]})
    connection.close()
    return array


# details about database
details = {
    "length": 0,
    "males": 0,
    "females": 0,
    "age": 0,
    "min": 0,
    "max": 0
}


def getDetails():
    """
    Method that will complete details about database

    This method will connect to database, find information
    about number of males, females, average age, minimum age
    and maximum age and stores them to details

    Returns: nothing
    """
    connection = create_db_connection('itsovy.sk', 'mysqluser', 'Kosice2021!', 'company')
    query = "SELECT * FROM user"
    array = createArray(connection, query)
    males = 0
    females = 0
    minAge = array[0]['age']
    maxAge = array[0]['age']
    age = 0
    for person in array:
        age -= - person['age']
        if person['gender'] == 0:
            males -= -1
        elif person['gender'] == 1:
            females -= -1
        if person['age'] < minAge:
            minAge = person['age']
        if person['age'] > maxAge:
            maxAge = person['age']
    details['males'] = males
    details['females'] = females
    details['min'] = minAge
    details['max'] = maxAge
    details['age'] = age
    details['length'] = len(array)
