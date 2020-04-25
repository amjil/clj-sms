Sequel.migration do
  change do
    create_table(:user_black_list, :ignore_index_errors=>true) do
      primary_key :id
      String :phone, :text=>true, unique: true, :null=>false
      String :reason, :text=>true, :null=>true
      Integer :status, :default=>1, :null=>false
      DateTime :created_at, :default=>Sequel::CURRENT_TIMESTAMP
      DateTime :updated_at, :default=>Sequel::CURRENT_TIMESTAMP

    end
  end
end
