from http.server import BaseHTTPRequestHandler, HTTPServer
import sys 

class SimpleHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()
        response_text = f"Hello from Python Backend on Port {self.server.server_port}"
        self.wfile.write(response_text.encode('utf-8'))

def run(port=8080):
    server_address = ('', port)
    httpd = HTTPServer(server_address, SimpleHandler)
    print(f"Starting Python Backend on port {port}...")
    httpd.serve_forever()

if __name__ == "__main__":
    if len(sys.argv) > 1:
        port = int(sys.argv[1]) # user provide a port
    else:
        port = 8081 # default
        
    run(port)