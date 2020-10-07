This is a database containing information about all fingerprint images that have been seen so far.  The goal is to use
it to detect if a fingerprint image is being reused, as this would indicate possible fraud.

If you're developing locally and you want to update the schema you'll have to blow away the existing image, container,
and volume so that the postgres instance doesn't persist on your host machine

```
docker-compose rm -f
docker rm identity-intelligence-db
docker rmi identity-intelligence-db
docker volume rm identity-intelligence_db_db_data
```
