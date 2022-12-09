default:
	echo "Rule not found"

up:
	docker compose up

down:
	docker compose down

bash_%:
	docker compose exec $(subst bash_,,$@) /bin/bash

ash_%:
	docker compose exec $(subst bash_,,$@) /bin/ash