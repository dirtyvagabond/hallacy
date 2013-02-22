hallacy
=======

Webservice for Chris Hallacy's Factual Hackathon project

To run locally:

```
lein2 run 3000
```

To redeploy to Heroku:
```
git push heroku master
heroku ps:scale web=1
heroku open
```

Example local request:
```
http://localhost:3000/?longitude=-118.418249&latitude=34.060106&plain=true
```