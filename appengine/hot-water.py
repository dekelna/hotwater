import cgi
import webapp2

from google.appengine.ext import db

class State(db.Model):
    state = db.BooleanProperty()

class MainPage(webapp2.RequestHandler):
    def get(self):
        state = False
        try:
            state = db.get(db.Key.from_path('State', 'state')).state
        except:
            pass
                
        self.__show_response(state)

    def post(self):
        new_state = True if 'True' == cgi.escape(self.request.get('state')) else False
        State(key_name='state' ,state=new_state).put()
        self.__show_response(new_state)
        
    def __show_response(self, state):
        state_color = 'green' if state else 'red'
        state_name = 'ON' if state else 'OFF'
        on_checked  = 'checked' if state else ''
        off_checked = '' if state else 'checked'
        
        self.response.out.write("""
          <html>
            <head><title>Dekel's Boiler</title></head>
            <body>
              <div>Current state is <text style="color:%s"><b>%s</b></div></div>
              <form action="/" method="post"><b>    
                <div style="color:green"><input type="radio" name="state" value="True" %s>ON</div>
                <div style="color:red"><input type="radio" name="state" value="False" %s>OFF</div>
                <input type="submit" value="Change">
              </b></form>
            </body>
          </html>""" % (state_color, state_name, on_checked, off_checked))

class Simple(webapp2.RequestHandler):
    def get(self):
        state = False
        try:
            state = db.get(db.Key.from_path('State', 'state')).state
        except:
            pass
        self.response.out.write('on' if state else 'off')

app = webapp2.WSGIApplication([('/', MainPage), ('/simple', Simple)], debug=True)
