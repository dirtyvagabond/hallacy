hallacy
=======

Webservice for Chris Hallacy's Factual Hackathon project

h3. To run locally

```bash
lein run 3000
```

Example local request:
```
http://localhost:3000/?longitude=-118.418249&latitude=34.060106&plain=true
```

h3. To redeploy to Heroku:
```
git push heroku master
heroku ps:scale web=1
heroku open
```

When running on Heroku, you must first configure your app's environment variables with your Factual API credentials.

Like:

```bash
heroku config:add FACT_KEY=YOUR_KEY
heroku config:add FACT_SECRET=YOUR_SECRET
```
