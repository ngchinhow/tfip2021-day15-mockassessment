# Available Endpoints

## List Books
### Endpoint 
`GET /books`
### Query Parameters
- pageNumber. Optional. Defaults to 1
- pageSize. Optional. Defaults to 100
## Search for a Book
### Endpoint 
`GET /search`
### Query Parameters
- q. Required. String to search for in Book title or author.
- pageNumber. Optional. Defaults to 1
- pageSize. Optional. Defaults to 100
## Get one Book
### Endpoint 
`GET /books/{id}`
### Path Parameter
- id. Required. ID of Book to retieve.
## Save a Book
### Endpoint
`POST /book`
### Request Body
- title. Optional. Title of Book.
- author. Optional. Author of Book.
## Update a Book
### Endpoint
`PUT /book/{id}`
### Request Body
- title. Optional. Title of Book.
- author. Optional. Author of Book.