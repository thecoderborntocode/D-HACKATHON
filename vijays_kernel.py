from flask import Flask, request, jsonify
import winsound
app = Flask(__name__)

@app.route('/wearos', methods=['POST'])  
def wearos():
    
    data = request.data.decode('utf-8')
    if(data == "1"):
         print("yes")
    print(data)
    
    
    return jsonify({"message": "Data received", "data": data}), 200
@app.route('/alarm',methods = ['GET'])
def alarm():
    
        frequency = 1000  
        duration = 500  # raises alaram if watch is removed from the wrist
        winsound.Beep(frequency, duration)

if __name__ == '__main__':
    app.run(debug=True, host='10.100.51.249', port=5000)
