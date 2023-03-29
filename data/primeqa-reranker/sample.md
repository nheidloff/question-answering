curl -X 'POST' \
 'http://9.47.193.65:50052/RerankRequest' \
 -H 'accept: application/json' \
 -H 'Content-Type: application/json' \
 -d '{
  "reranker": {
  "reranker_id": "ColBERTReranker",
  "parameters": [
    {
      "parameter_id": "model",
      "value": "/u/franzm/git8/PrimeQA_dpr/primeqa/store/checkpoints/drdecr/colbert.dnn.batch_72000.model"
    }
  ]
  },
  "queries": [
  "How to I position an index?"
  ],
  "hitsperquery": [ 
  [
    {
    "document": {
      "text": "Indexes on partitioned tables The following types of indexes apply to only partitioned tables: partitioned indexes, partitioning indexes (PIs), data-partitioned secondary indexes (DPSIs), and nonpartitioned secondary indexes (NPIs or NPSIs). How Db2 implicitly creates an index In certain circumstances, Db2 implicitly creates the unique indexes that are used to enforce the uniqueness of the primary keys or unique keys. Db2 13 - Administration - Implementing Db2 indexes,Parent topic: Altering Db2 indexes Related concepts Alternative method for altering an index Indexes that are padded or not padded Related tasks Adding columns to an index Altering the clustering of an index Dropping and redefining a Db2 index Reorganizing indexes Related reference ALTER INDEX Db2 13 - Administration - Altering how varying-length index columns are stored.",
      "document_id": "0",
      "title": "Indexes"
    },
    "score": 1.4
    },
    {
    "document": {
      "text": "Bind options for remote access Binding a package to run at a remote location is like binding a package to run at your local Db2 subsystem. Binding a plan to run the package is like binding any other plan. However, a few differences exist. For the general instructions, see Preparing an application to run on Db2 for z/OS. BIND PLAN options for    Db2 13 - Application programming and SQL - Bind options for remote access. Bind options for remote access",
      "document_id": "1",
      "title": "Remote access"
    },
    "score": 1.4
    }
  ]
 ]
}'


Output:
[
  [
    {
      "document": {
        "text": "Indexes on partitioned tables The following types of indexes apply to only partitioned tables: partitioned indexes, partitioning indexes (PIs), data-partitioned secondary indexes (DPSIs), and nonpartitioned secondary indexes (NPIs or NPSIs).  How Db2 implicitly creates an index In certain circumstances, Db2 implicitly creates the unique indexes that are used to enforce the uniqueness of the primary keys or unique keys. Db2 13 - Administration - Implementing Db2 indexes,Parent topic: Altering Db2 indexes Related concepts Alternative method for altering an index Indexes that are padded or not padded Related tasks Adding columns to an index Altering the clustering of an index Dropping and redefining a Db2 index Reorganizing indexes Related reference ALTER INDEX Db2 13 - Administration - Altering how varying-length index columns are stored.",
        "document_id": "0",
        "title": "Indexes"
      },
      "score": 22.14895248413086
    },
    {
      "document": {
        "text": "Bind options for remote access Binding a package to run at a remote location is like binding a package to run at your local Db2 subsystem. Binding a plan to run the package is like binding any other plan. However, a few differences exist. For the general instructions, see Preparing an application to run on Db2 for z/OS.  BIND PLAN options for       Db2 13 - Application programming and SQL - Bind options for remote access. Bind options for remote access",
        "document_id": "1",
        "title": "Remote access"
      },
      "score": 13.350204467773438
    }
  ]
]