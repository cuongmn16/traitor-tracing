export interface ApiResponse<T>{
    code: number;
    message?: string;
    result?: T;
}
  export interface ImageRequest {
    fileName: string;
    filePath: string;
    phash: string;
  }
   
  export interface UploadProgress {
    progress: number;
    data?: ImageResponse;
  }

  export interface ImageResponse {
    id: string; 
    fileName: string;
    filePath: string;  
    imageUrl?: string;
    phash: string;    
    createdAt: string; 
  }