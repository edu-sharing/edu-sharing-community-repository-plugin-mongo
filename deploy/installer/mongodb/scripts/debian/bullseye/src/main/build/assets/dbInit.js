	db.createUser({
		user: 'repository',
		pwd: 'repository',
		roles: [ { role: 'root', db: 'admin' } ]
	})