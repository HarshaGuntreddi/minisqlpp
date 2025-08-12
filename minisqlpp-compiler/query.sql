SELECT u.name, o.total FROM users u, orders o WHERE u.id = o.user_id ORDER BY o.total DESC LIMIT 2;
