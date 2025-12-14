# Image Management API Documentation

## Base URL
```
http://localhost:8100/api/v1/images
```

## Authentication
- **Upload, Update, Delete**: Requires authentication (JWT Bearer Token)
- **Get List, Get Details**: Public access (access control handled in service layer)

---

## Endpoints

### 1. POST /api/v1/images/upload - Upload Image

Upload a single image file.

**Authentication**: Required

**Request**:
- Content-Type: `multipart/form-data`
- Body:
  - `file` (required): Image file (jpg, jpeg, png, gif, webp, bmp)
  - `description` (optional): Image description
  - `isPublic` (optional, default: false): Make image publicly accessible

**Validation**:
- File must be an image (content-type starts with "image/")
- Supported formats: jpg, jpeg, png, gif, webp, bmp
- Max file size: 10MB

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "id": 1,
    "originalFilename": "house.jpg",
    "storedFilename": "f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "fileUrl": "http://localhost:8100/api/v1/files/download/f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "contentType": "image/jpeg",
    "fileSize": 1048576,
    "fileSizeFormatted": "1.0 MB",
    "fileType": "IMAGE",
    "uploadedBy": "admin",
    "entityType": "IMAGE",
    "description": "House photo",
    "isPublic": false,
    "thumbnailUrl": "http://localhost:8100/api/v1/files/download/thumb_f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "width": 1920,
    "height": 1080,
    "createdAt": "2024-12-14 20:00:00",
    "updatedAt": "2024-12-14 20:00:00"
  },
  "timestamp": "2024-12-14T20:00:00"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8100/api/v1/images/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "description=House photo" \
  -F "isPublic=false"
```

---

### 2. GET /api/v1/images - Get Images List

Get paginated list of images with optional filters.

**Authentication**: Not required (public access, but filtered by permissions)

**Query Parameters**:
- `page` (optional, default: 0): Page number (0-based)
- `size` (optional, default: 20): Page size
- `isPublic` (optional): Filter by public images only (true/false)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Images retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "originalFilename": "house.jpg",
        "fileUrl": "http://localhost:8100/api/v1/files/download/...",
        "thumbnailUrl": "http://localhost:8100/api/v1/files/download/thumb_...",
        "width": 1920,
        "height": 1080,
        "fileSize": 1048576,
        "isPublic": true,
        "uploadedBy": "admin",
        "createdAt": "2024-12-14 20:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  },
  "timestamp": "2024-12-14T20:00:00"
}
```

**cURL Example**:
```bash
# Get all images (with access control)
curl -X GET "http://localhost:8100/api/v1/images?page=0&size=20"

# Get only public images
curl -X GET "http://localhost:8100/api/v1/images?isPublic=true&page=0&size=20"
```

---

### 3. GET /api/v1/images/{id} - Get Image Details

Get detailed information about a specific image.

**Authentication**: Not required (public access, but access controlled in service)

**Path Parameters**:
- `id` (required): Image ID

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Image retrieved successfully",
  "data": {
    "id": 1,
    "originalFilename": "house.jpg",
    "storedFilename": "f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "fileUrl": "http://localhost:8100/api/v1/files/download/f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "contentType": "image/jpeg",
    "fileSize": 1048576,
    "fileSizeFormatted": "1.0 MB",
    "fileType": "IMAGE",
    "uploadedBy": "admin",
    "entityType": "IMAGE",
    "description": "House photo",
    "isPublic": false,
    "thumbnailUrl": "http://localhost:8100/api/v1/files/download/thumb_f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "width": 1920,
    "height": 1080,
    "createdAt": "2024-12-14 20:00:00",
    "updatedAt": "2024-12-14 20:00:00"
  },
  "timestamp": "2024-12-14T20:00:00"
}
```

**Error**: `404 Not Found` - Image not found or not accessible

**cURL Example**:
```bash
curl -X GET http://localhost:8100/api/v1/images/1
```

---

### 4. PUT /api/v1/images/{id} - Update Image Metadata

Update description and visibility of an image.

**Authentication**: Required (must be image owner)

**Path Parameters**:
- `id` (required): Image ID

**Query Parameters**:
- `description` (optional): New description
- `isPublic` (optional): Make image public (true/false)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Image updated successfully",
  "data": {
    "id": 1,
    "description": "Updated description",
    "isPublic": true,
    ...
  },
  "timestamp": "2024-12-14T20:00:00"
}
```

**Error**: 
- `404 Not Found` - Image not found
- `403 Forbidden` - Not the image owner

**cURL Example**:
```bash
curl -X PUT "http://localhost:8100/api/v1/images/1?description=New description&isPublic=true" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 5. DELETE /api/v1/images/{id} - Delete Image

Delete an image and its metadata.

**Authentication**: Required (must be image owner or admin)

**Path Parameters**:
- `id` (required): Image ID

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Image deleted successfully",
  "data": null,
  "timestamp": "2024-12-14T20:00:00"
}
```

**Error**: 
- `404 Not Found` - Image not found
- `403 Forbidden` - Not the image owner or admin

**cURL Example**:
```bash
curl -X DELETE http://localhost:8100/api/v1/images/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Access Control

### Public Images
- Images with `isPublic: true` can be accessed by anyone (no authentication required)
- Appears in public image lists

### Private Images
- Images with `isPublic: false` can only be accessed by:
  - The image owner (uploader)
  - Admin users
- Not visible in public image lists

---

## Image Processing

When an image is uploaded, the system automatically:
1. **Detects dimensions**: Extracts width and height
2. **Resizes if needed**: If image exceeds 2048x2048, it's resized
3. **Creates thumbnail**: Generates a 300x300 thumbnail for preview
4. **Stores metadata**: Saves all information to database

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "File must be an image. Content type: application/pdf",
  "data": null,
  "timestamp": "2024-12-14T20:00:00"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Image not found with ID: 999",
  "data": null,
  "timestamp": "2024-12-14T20:00:00"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "You can only update your own files",
  "data": null,
  "timestamp": "2024-12-14T20:00:00"
}
```

---

## Integration Notes

- Images are stored using the existing `FileManagementService`
- All images are stored in the `file_metadata` table with `file_type = 'IMAGE'`
- Image files are stored in the file storage system (local or S3)
- Thumbnails are automatically generated for faster loading
- Access control is handled at the service layer

---

## Related Endpoints

- `/api/v1/files/download/{filename}` - Download image file
- `/api/v1/files/view/{filename}` - View image in browser
- `/api/v1/files/{id}` - Get file metadata (works for images too)

