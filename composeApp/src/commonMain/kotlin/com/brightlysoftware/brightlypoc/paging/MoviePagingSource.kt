//package com.brightlysoftware.brightlypoc.paging
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.brightlysoftware.brightlypoc.models.Movie
//import com.brightlysoftware.brightlypoc.repository.MovieRepository
//
//class MoviePagingSource(
//    private val repository: MovieRepository
//) : PagingSource<Int, Movie>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
//        return try {
//            val page = params.key ?: 1
//            val response = repository.getPopularMovies(page)
//
//            response.fold(
//                onSuccess = { movieResponse ->
//                    LoadResult.Page(
//                        data = movieResponse.results,
//                        prevKey = if (page == 1) null else page - 1,
//                        nextKey = if (page >= movieResponse.totalPages) null else page + 1
//                    )
//                },
//                onFailure = { exception ->
//                    LoadResult.Error(exception)
//                }
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//        }
//    }
//}