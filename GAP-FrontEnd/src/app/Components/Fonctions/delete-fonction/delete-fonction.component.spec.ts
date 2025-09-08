import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteFonctionComponent } from './delete-fonction.component';

describe('DeleteFonctionComponent', () => {
  let component: DeleteFonctionComponent;
  let fixture: ComponentFixture<DeleteFonctionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteFonctionComponent]
    });
    fixture = TestBed.createComponent(DeleteFonctionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
