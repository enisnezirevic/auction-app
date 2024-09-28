import {Injectable} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {BehaviorSubject, Observable, Subscription} from "rxjs";
import {Constant} from "../models/enums/constant";
import {Api} from "./api.service";
import Category = Api.CategoryApi.Category;
import Subcategory = Api.CategoryApi.Subcategory;

@Injectable({
  providedIn: "root"
})
export class CategoryService {
  private activeCategory = new BehaviorSubject<string>(Constant.EmptyValue);
  private activeSubcategory = new BehaviorSubject<string>(Constant.EmptyValue);
  private categoriesSubject = new BehaviorSubject<Array<Category> | undefined>(undefined);
  private categoriesSet = new Set<string>();
  private subscription: Record<string, Subscription> = {};


  constructor(private router: Router, private apiService: Api.Service) {
  }

  initCategories(): void {
    this.apiService.getAllCategories().subscribe((categories: Array<Category>): void => {
      this.categoriesSubject.next(categories);
    });
  }

  getActiveCategory(): Observable<string> {
    return this.activeCategory.asObservable();
  }

  getActiveSubcategory(): Observable<string> {
    return this.activeSubcategory.asObservable();
  }

  getAllCategories(): Observable<Array<Category> | undefined> {
    return this.categoriesSubject.asObservable();
  };

  handleCategoryChange(paramCategory: string, activatedRoute: ActivatedRoute): void {
    if (this.activeCategory.getValue() === paramCategory.toLowerCase()) return;

    this.categoryExists(paramCategory)
      ? this.activeCategory.next(paramCategory.toLowerCase())
      : this.redirectToDefaultCategory(activatedRoute);
  }

  handleSubcategoryChange(paramCategory: string): void {
    this.activeSubcategory.next(paramCategory ?? Constant.EmptyValue);
  }


  subscribeToCategories(): void {
    if (this.categoriesSubject) {
      this.subscription["category"] = this.categoriesSubject.subscribe(value => {
        this.mapCategoriesToSet(value);
      });
    }
  }

  resetActiveCategories(): void {
    this.activeCategory.next(Constant.EmptyValue);
    this.activeSubcategory.next(Constant.EmptyValue);
  }

  public findCategoryById(categoryId: number): string {
    const findCategory = (categories: Category[] | Subcategory[]): string => {
      for (const category of categories) {
        if (category.id === categoryId) {
          return category.name;
        }
        if ("subcategories" in category && category.subcategories && category.subcategories.length > 0) {
          const subcategoryResult = findCategory(category.subcategories);
          if (subcategoryResult !== Constant.EmptyValue) {
            return subcategoryResult;
          }
        }
      }
      return Constant.EmptyValue;
    };

    const categories: Array<Category> | undefined = this.categoriesSubject.getValue();

    if (categories) {
      const result: string = findCategory(categories);
      return result !== Constant.EmptyValue ? result : Constant.EmptyValue;
    }

    return Constant.EmptyValue;
  }

  private mapCategoriesToSet(value: Category[] | undefined): void {
    value?.map(category => this.categoriesSet.add(category.name.toLowerCase()));
  }

  private categoryExists(section: string): boolean {
    return this.categoriesSet.has(section);
  }

  private redirectToDefaultCategory(activatedRoute: ActivatedRoute): void {
    this.router.navigate([], {queryParams: {category: null, subcategory: null}, relativeTo: activatedRoute}).then(null);
  }
}
