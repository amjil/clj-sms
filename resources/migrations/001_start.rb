Sequel.migration do
  change do
    create_table(:user_sms_log, :ignore_index_errors=>true) do
      primary_key :id
      String :phone, :text=>true, :null=>false
      String :sms, :text=>true, :null=>false
      Integer :status, :default=>0, :null=>false
      DateTime :created_at, :default=>Sequel::CURRENT_TIMESTAMP
      DateTime :updated_at, :default=>Sequel::CURRENT_TIMESTAMP
      
      index [:phone]
    end
  end
end
