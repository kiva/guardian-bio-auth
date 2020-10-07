This is a database instance kiva_wallet_sync and kiva_biometric_template records will be accessed via identity-service.

If you're developing locally and you want to update the schema you'll have to blow away the existing image, container
and volume so that the postgres instance doesn't persist on your host machine.

```
docker-compose rm -f
docker rm identity-template-db
docker rmi identity-template-db
docker volume rm identity-template_db_db_data
```
