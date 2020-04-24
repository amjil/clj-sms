sequel -d postgres://host/database > db/migrations/001_start.rb
sequel -m db/migrations postgres://host/database
