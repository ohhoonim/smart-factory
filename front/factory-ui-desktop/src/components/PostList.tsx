import { useQuery } from "@tanstack/react-query";
import { Link } from "@tanstack/react-router";
import axios from "axios";

const fetchPosts = async () => {
  const response = await axios.get(
    "https://jsonplaceholder.typicode.com/posts"
  );
  return response.data;
};

interface Post {
  id: string;
  title: string;
}

function PostList() {
  const {
    data: posts,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["posts"], // 쿼리 키 (캐싱 및 재요청을 위한 고유 식별자)
    queryFn: fetchPosts, // 데이터를 가져오는 비동기 함수
  });

  if (isLoading) {
    return <div>로딩 중입니다... 🔄</div>;
  }

  if (isError) {
    return <div>에러 발생: {error.message} 🛑</div>;
  }

  return (
    <div>
      <h1>게시글 목록</h1>
      <ul>
        {posts.map((post: Post) => (
          <li key={post.id}>
            <Link to="/practice/posts/$id" params={{ id: post.id }}>
              {post.title}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default PostList;
